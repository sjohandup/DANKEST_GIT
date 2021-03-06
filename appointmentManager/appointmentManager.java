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
		out.println("Welcome to your very own Appointment Manager");
		out.println("=====================================================");
		out.println(msg);
		if(qList != null)
		{
			for(int i=0; i < qList.size(); i++)
			{
				out.print((10+i));
				out.print(qList.get(i));
			}
		}
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
		out.println("Welcome to your very own Appointment Manager");
		out.println("=====================================================");
		out.println("What would you like to do?");
		out.println("1. View your appointments");
		out.println("2. Add a new appointment");
		out.println("3. Update an appointment");
		out.println("4. Remove appointment");
		out.println("5. Search appointment");
		out.println("6. Exit server");
		out.println("=====================================================");
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