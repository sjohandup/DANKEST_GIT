/* Created by:
 * ===========
 * SJ du Plooy 	<12070794>
 * M Peroski 	<13242475>
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class appointmentManager
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket socket = null;				//for handling the active socket
		Socket connection = null;				//for handling the active connection
		PrintStream outgoing = null;			//used to broadcast outgoing message to client
		BufferedReader incoming = null;			//used to read incoming message from client
		String msg = null;						//used to store the message between client & server
		appointmentList appointments = null;	//used to interact with a list of appointments
		
		try
		{
			boolean exit = false;
			//creating server socket, @param1 = port number @param2 = backlog
			socket = new ServerSocket(8190 , 10);
			display("Server is running on port 8190...");
			
			connection = socket.accept();
			display("Connection established from " + connection.getInetAddress().getHostName() + " : " + connection.getPort());
			
			//get Input and Output streams
			outgoing = new PrintStream(connection.getOutputStream());
			outgoing.flush();
			
			//create new BufferedReader using ANSI character encoding
			incoming = new BufferedReader(new InputStreamReader(connection.getInputStream(), "Cp1252"));
			
			appointments = new appointmentList();
			//appointments.loadList();
			
			//Send header to browser
			printHTMLHeader(outgoing);
			
			do
			{
				//creating menu:
				drawMenu(outgoing);
				
				//read input from client
				msg = (String)incoming.readLine();
				
				if(msg != null)
				{
					display("client response: "+msg);
					String text = null;
					
					switch(msg)
					{
						case "1":
						{
							text = "View your appointments";
							exit = view(incoming, outgoing, text, appointments);
							break;
						}
						case "2":
						{
							text = "Add a new appointment";
							add(incoming, outgoing, text, appointments);
							break;
						}
						case "3":
						{
							text = "Update an appointment";
							exit = update(incoming, outgoing, text, appointments);
							break;
						}
						case "4":
						{
							text = "Remove appointment";
							remove(incoming, outgoing, text, appointments);
							break;
						}
						case "5":
						{
							text = "Search appointment";
							exit = search(incoming, outgoing, text, appointments);
							break;
						}
						case "6":
						{
							text = "It's been fun XD";
							sendMsg(outgoing, text, null);
							exit = true;
							break;
						}
						default:
						{
							text = "Incorrect value!  Please enter a number between 1 & 6";
							break;
						}
					}
					
					//display(text);
					//sendMsg(outgoing, text, queryList);
				}
				else
				{
					display("Client has disconnected");
                    break;
				}
			}
			while(!exit);
			printHTMLFooter(outgoing);
			//appointments.saveList();
		}
		catch(IOException e)
		{
			System.err.println("Error: Input Output Exception");
		}
		finally
		{
			try
			{
				appointments.saveList();
				incoming.close();
				outgoing.close();
				socket.close();
			}
			catch(IOException ioException)
			{
				System.err.println("Error: Unable to close due to IO exception");
			}
		}
	}
	
	private static void printHTMLHeader(PrintStream out)
	{
		/*
		out.println("HTTP/1.1 200 OK");
		out.println("Content-Type: text/html");
		out.println("\r\n");
		out.println("<p> Hello world </p>");
		out.flush();
		*/
		
		out.println
			("HTTP/1.1 200 OK\r\n" +
			"Server: javaServer (windows NT 10.0) mod_auth_pam/1.1.1 \r\n" +
			"Vary: Accept-Encoding, User" + 
			"Content-Type: text/html\r\n" +
			"\r\n" +
			"<!DOCTYPE HTML PUBLIC " +
			"\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
			"<html lang='en'>" +
			"<head>\n" +
			" <title>Appointment Manager</title>\n" +
			"</head>\n" +
			"<body>\n"
		);
	}
	
	private static void printHTMLFooter(PrintStream out)
	{
		out.println
			("</body>\n" +
			"</html>\n"
		);
	}
	
	private static void display(String message)
	{
		System.out.println(message);
	}
	
	private static void sendMsg(PrintStream out, String msg, ArrayList<String> qList)
	{
		out.write(27);
		out.println("[2J");
		out.write(27);
		out.print("[37m");
		out.write(27);
		out.println("[5;5HWelcome to your very own Appointment Manager");
		out.write(27);
		out.println("[6;0H=====================================================");
		out.write(27);
		out.print("[7;5H");
		out.write(27);
		out.println("[33m"+msg);
		if(qList != null)
		{
			for(int i=0; i < qList.size(); i++)
			{
				out.write(27);
				out.print("["+(10+i)+";0H");
				
				out.write(27);
				out.print("[34m"+qList.get(i));
				
				out.write(27);
				out.println("[0m");
			}
		}
		out.write(27);
		out.print("[0m");
		out.flush();
		sendMsgServerSide(msg, qList);
	}
	private static void sendMsgServerSide(String msg, ArrayList<String> qList)
	{
		System.out.println("Welcome to your very own Appointment Manager");
		System.out.println("=====================================================");
		System.out.println(msg);
		if(qList != null && !qList.isEmpty())
		{
			for(int i=0; i < qList.size(); i++)
			{
				System.out.println(qList.get(i));
			}
		}
	}
	
	private static void drawMenu(PrintStream out)
	{
		//build & send Menu to client
		out.write(27);
		out.println("[2J");
		out.write(27);
		out.print("[37m");
		out.write(27);
		out.println("[5;5HWelcome to your very own Appointment Manager");
		out.write(27);
		out.println("[6;0H=====================================================");
		out.write(27);
		out.print("[34m");
		out.write(27);
		out.println("[7;5HWhat would you like to do?");
		out.write(27);
		out.print("[36m");
		out.write(27);
		out.println("[8;5H1. View your appointments");
		out.write(27);
		out.println("[9;5H2. Add a new appointment");
		out.write(27);
		out.println("[10;5H3. Update an appointment");
		out.write(27);
		out.println("[11;5H4. Remove appointment");
		out.write(27);
		out.println("[12;5H5. Search appointment");
		out.write(27);
		out.println("[13;5H6. Exit server");
		out.write(27);
		out.print("[37m");
		out.write(27);
		out.println("[14;0H=====================================================");
		out.write(27);
		out.print("[0m");
		out.flush();
		drawMenuServerSide();
	}
	private static void drawMenuServerSide()
	{
		//build & display Menu on server
		System.out.println("Welcome to your very own Appointment Manager");
		System.out.println("=====================================================");
		System.out.println("What would you like to do?");
		System.out.println("1. View your appointments");
		System.out.println("2. Add a new appointment");
		System.out.println("3. Update an appointment");
		System.out.println("4. Remove appointment");
		System.out.println("5. Search appointment");
		System.out.println("6. Exit server");
		System.out.println("=====================================================");
	}
	
	private static boolean view(BufferedReader in, PrintStream out, String msg, appointmentList appointments)
	{
		ArrayList<String> queryList = new ArrayList<String>();
		queryList = appointments.view();
		
		queryList.add("1. Back to main Menu");
		queryList.add("2. Exit server");
		
		//display(msg);
		sendMsg(out, msg, queryList);
		
		try
		{
			//read input from client
			msg = (String)in.readLine();
			
			if(msg != null) {
				display("client response: "+msg);
				switch(msg) {
					case "1": {
						return false;
					}
					case "2": {
						return true;
					}
				}
			}
		}
		catch(IOException e)
		{
			System.err.println("Error: Input Output Exception");
		}
		return false;
	}
	private static boolean add(BufferedReader in, PrintStream out, String msg, appointmentList appointments)
	{
		String newDate = null;
		String newTime = null;
		String newWith = null;
		String aptString = null;
		ArrayList<String> queryList = new ArrayList<String>();
		
		//getting new date
		queryList.add("What is the Date of the appointment?");
		
		//display(msg);
		sendMsg(out, msg, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newDate = msg;
				aptString = "On "+msg+" at ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		//getting new time
		queryList.add("What is the Time of the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newTime = msg;
				aptString += msg+" with ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		//getting new with
		queryList.add("With whoom is the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newWith = msg;
				aptString += msg;
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		
		appointments.addApt(newDate, newTime, newWith);
		
		return false;
	}
	private static boolean remove(BufferedReader in, PrintStream out, String msg, appointmentList appointments)
	{
		String newDate = null;
		String newTime = null;
		String newWith = null;
		String aptString = null;
		ArrayList<String> queryList = new ArrayList<String>();
		
		queryList = appointments.view();
		
		//getting new date
		queryList.add("What is the Date of the appointment?");
		
		//display(msg);
		sendMsg(out, msg, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newDate = msg;
				aptString = "On "+msg+" at ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		queryList = appointments.view();
		//getting new time
		queryList.add("What is the Time of the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newTime = msg;
				aptString += msg+" with ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		queryList = appointments.view();
		//getting new with
		queryList.add("With whoom is the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newWith = msg;
				aptString += msg;
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		
		appointments.removeApt(newDate, newTime, newWith);
		
		return false;
	}
	private static boolean search(BufferedReader in, PrintStream out, String msg, appointmentList appointments)
	{
		String newDate = null;
		String newTime = null;
		String newWith = null;
		String aptString = null;
		ArrayList<String> queryList = new ArrayList<String>();
		
		queryList = appointments.view();
		
		//getting new date
		queryList.add("What is the Date of the appointment?");
		
		//display(msg);
		sendMsg(out, msg, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newDate = msg;
				aptString = "On "+msg+" at ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		queryList = appointments.view();
		//getting new time
		queryList.add("What is the Time of the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newTime = msg;
				aptString += msg+" with ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		queryList = appointments.view();
		//getting new with
		queryList.add("With whoom is the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				newWith = msg;
				aptString += msg;
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		
		if(appointments.search(newDate, newTime, newWith))
		{
			msg = "Appointment Found!";
		}
		else
		{
			msg = "No such appointment found";
		}
		
		queryList.clear();
		queryList = appointments.view();
		
		queryList.add("1. Back to main Menu");
		queryList.add("2. Exit server");
		
		sendMsg(out, msg, queryList);
		
		try {
			//read input from client
			msg = (String)in.readLine();
			
			if(msg != null) {
				display("client response: "+msg);
				switch(msg) {
					case "1": {
						return false;
					}
					case "2": {
						return true;
					}
				}
			}
		} catch(IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		return false;
	}
	private static boolean update(BufferedReader in, PrintStream out, String msg, appointmentList appointments)
	{
		String oldDate = null;
		String oldTime = null;
		String oldWith = null;
		
		String aptString = null;
		
		String newDate = null;
		String newTime = null;
		String newWith = null;
		ArrayList<String> queryList = new ArrayList<String>();
		
		queryList = appointments.view();
		
		//getting new date
		queryList.add("What is the Date of the appointment?");
		
		//display(msg);
		sendMsg(out, msg, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				oldDate = msg;
				aptString = "On "+msg+" at ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		queryList = appointments.view();
		//getting new time
		queryList.add("What is the Time of the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				oldTime = msg;
				aptString += msg+" with ";
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		queryList.clear();
		queryList = appointments.view();
		//getting new with
		queryList.add("With whoom is the appointment?");
		
		//display(msg);
		sendMsg(out, aptString, queryList);
		
		try {
			msg = (String)in.readLine();
			if(msg != null) {
				display("client response: "+msg);
				oldWith = msg;
				aptString += msg;
			}
		} catch (IOException e) {
			System.err.println("Error: Input Output Exception");
		}
		
		if(appointments.search(oldDate, oldTime, oldWith))
		{
			msg = "Appointment Found!";
			
			queryList.clear();
			queryList = appointments.view();
			
			//getting new date
			queryList.add("What is the New Date of the appointment?");
			
			//display(msg);
			sendMsg(out, msg, queryList);
			
			try {
				msg = (String)in.readLine();
				if(msg != null) {
					display("client response: "+msg);
					newDate = msg;
					aptString = "On "+msg+" at ";
				}
			} catch (IOException e) {
				System.err.println("Error: Input Output Exception");
			}
			queryList.clear();
			queryList = appointments.view();
			//getting new time
			queryList.add("What is the New Time of the appointment?");
			
			//display(msg);
			sendMsg(out, aptString, queryList);
			
			try {
				msg = (String)in.readLine();
				if(msg != null) {
					display("client response: "+msg);
					newTime = msg;
					aptString += msg+" with ";
				}
			} catch (IOException e) {
				System.err.println("Error: Input Output Exception");
			}
			queryList.clear();
			queryList = appointments.view();
			//getting new with
			queryList.add("With whoom is the New appointment?");
			
			//display(msg);
			sendMsg(out, aptString, queryList);
			
			try {
				msg = (String)in.readLine();
				if(msg != null) {
					display("client response: "+msg);
					newWith = msg;
				}
			} catch (IOException e) {
				System.err.println("Error: Input Output Exception");
			}
			
			if(appointments.updateApt(oldDate, oldTime, oldWith, newDate, newTime, newWith))
			{
				msg = "No such appointment found";
			}
			else
			{
				msg = "No such appointment found";
			}
		}
		else
		{
			msg = "No such appointment found";
		}
		queryList.clear();
		queryList.add("1. Back to main Menu");
		queryList.add("2. Exit server");
		
		//display(msg);
		sendMsg(out, msg, queryList);
		
		try
		{
			//read input from client
			msg = (String)in.readLine();
			
			if(msg != null) {
				display("client response: "+msg);
				switch(msg) {
					case "1": {
						return false;
					}
					case "2": {
						return true;
					}
				}
			}
		}
		catch(IOException e)
		{
			System.err.println("Error: Input Output Exception");
		}
		return false;
	}
}







/*
	
	private void printHeader(PrintWriter out)
	{
		String serverName = "EchoServer";
		out.println
			("HTTP/1.0 200 OK\r\n" +
			"Server: " + serverName + "\r\n" +
			"Content-Type: text/html\r\n" +
			"\r\n" +
			"<HTML>\n" +
			"<!DOCTYPE HTML PUBLIC " +
			"\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
			"<HEAD>\n" +
			" <TITLE>" + serverName + " Results</TITLE>\n" +
			"</HEAD>\n" +
			"\n" +
			"<BODY BGCOLOR=\"#FDF5E6\">\n" +
			"<H1 ALIGN=\"CENTER\">" + serverName +
			" Results</H1>\n" +
			"Here is the request line and request headers\n" +
			"sent by your browser:\n" +
			"<PRE>"
		);
	}
*/