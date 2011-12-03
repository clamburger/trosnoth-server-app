package org.trosnoth.serveradmin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;

import android.os.Environment;
import android.util.Log;

public class AutomatedTelnetClient {
    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private String prompt = ">";
    
    @SuppressWarnings("unused")
    private String server;
    @SuppressWarnings("unused")
	private int port;
    private String password;

    public AutomatedTelnetClient(String server, int port, String password) throws IOException {
    	this.server = server;
    	this.port = port;
    	this.password = password;
    	
    	telnet.connect(server, port);
    }
       
    public boolean initalise() {
    
        // Get input and output stream references
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());

        // Log the user on
        readUntil("username: ");
        write("trosnoth");
        readUntil("password: ");
        write(password);
        
        // Advance to a prompt
        readUntil("\r\n");
        
        int oneChar = 0;

		try {
			oneChar = in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if (oneChar == -1) {
        	return false;
        }
        
        readAll();
        
        /*Log.i("Trosnoth!", readWrite("dir()"));
        Log.i("Trosnoth!", readWrite("'a'+'bc'"));
        Log.i("Trosnoth!", readWrite("len(authfactory.servers)"));*/
        
        return true;
    }
    
    public Object parse(String result) {
    	if (result.length() == 0) {
    		return "";
    	} else if (result.equals("True")) {
    		return true;
    	} else if (result.equals("False")) {
    		return false;
    	} else if (result.charAt(0) == '\'' || result.charAt(0) == '"') {
    		return result.substring(1, result.length()-1);
    	} else if (result.charAt(0) == 'u') {
    		return result.substring(2, result.length()-1);
    	} else if (result.charAt(0) == '[' || result.charAt(0) == '(') {
    		return null;
    	} else {
    		Double.valueOf(result);
    	}
    	return null;
    }
    
    public String readWrite(String command) {
    	// Send the command to the server
    	
    	//Log.v("Telnet", "Sending: " + command);
    	
    	write(command);
    	
    	// Read until the line break
    	//String result = readUntil("\r\n").trim();
    	String result = readUntil(">>>");
    	result = result.substring(0, result.length()-3).trim();
    	//result = result.trim();
    	
    	//Log.v("Telnet", "Received: " + result);
    	
    	// Flush any remaining text (including the next prompt)
    	readAll();
    	
    	//Log.v("Telnet", "Flush successful");
    	
    	return result;
    }
    
    public void send(String command) {
    	
    	//Log.v("Telnet", "Sending: " + command);
    	
    	write(command);
    	
    	readAll();
    }

    public String readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            char ch = (char) in.read();
            while (true) {
                sb.append(ch);
                
                if (sb.length() > 2000) {
                	File dir = Environment.getExternalStorageDirectory();
                	BufferedWriter out = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/fail"+System.currentTimeMillis()+".log"));
                	out.write(sb.toString());
                	out.close();
                	Log.e("Trosnoth", "Response exceeded 2,000 characters. Returning null.");
                	return null;
                }

                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                }
                ch = (char) in.read();
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    public String readAll() {
    	
    	StringBuffer sb = new StringBuffer();
		char ch;
		
    	try {
    		Thread.sleep(100);
    		while (in.available() > 0) {
    			ch = (char) in.read();
    			sb.append(ch);
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	return sb.toString();
    }

    public void write(String value) {
        try {
            out.print(value+"\r\n");
            out.flush();
            telnet.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String command) {
        try {
            write(command);
            return readUntil(prompt + " ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}