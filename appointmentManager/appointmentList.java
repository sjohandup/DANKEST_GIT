/* Created by:
 * ===========
 * SJ du Plooy 	<12070794>
 * M Peroski 	<13242475>
 */

import java.io.*;
import java.util.*;

public class appointmentList
{
	private class appointment
	{
		public String date;
		public String time;
		public String with;
		
		public appointment(String d, String t, String w)
		{
			date = d;
			time = t;
			with = w;
		}
	}
	
	ArrayList<appointment> aptList = null;
	ArrayList<String> tempStr = new ArrayList<String>();
	
	
	public appointmentList()
	{
		aptList = new ArrayList<appointment>();
		loadList();
	}
	
	public void addApt(String date, String time, String with)
	{
		appointment newAppointment = new appointment(date, time, with);
		aptList.add(newAppointment);
		//saveList();
	}
	public boolean removeApt(String date, String time, String with)
	{
		if(!aptList.isEmpty())
		{
			for(int i =0; i < aptList.size(); i++)
			{
				if(aptList.get(i).date.equals(date) && aptList.get(i).time.equals(time) && aptList.get(i).with.equals(with))
				{
					aptList.remove(i);
					//saveList();
					return true;
				}
			}
		}
		return false;
	}
	public boolean updateApt(String d1, String t1, String w1, String d2, String t2, String w2)
	{
		if(!aptList.isEmpty())
		{
			for(int i =0; i < aptList.size(); i++)
			{
				if(aptList.get(i).date.equals(d1) && aptList.get(i).time.equals(t1) && aptList.get(i).with.equals(w1))
				{
					aptList.get(i).date = d2;
					aptList.get(i).time = t2;
					aptList.get(i).with = w2;
					
					//saveList();
					return true;
				}
			}
		}
		return false;
	}
	public boolean search(String date, String time, String with)
	{
		if(!aptList.isEmpty())
		{
			appointment target = new appointment(date, time, with);
			for(int i =0; i < aptList.size(); i++)
			{
				if(aptList.get(i).date.equals(date) && aptList.get(i).time.equals(time) && aptList.get(i).with.equals(with))
					return true;
			}
		}
		return false;
	}
	public ArrayList<String> view()
	{
		ArrayList<String> result = new ArrayList<>();
		if(!aptList.isEmpty())
		{
			for(int i =0; i < aptList.size(); i++)
			{
				result.add(printApt(i, aptList.get(i)));
			}
		}
		else
			result.add("You currently have no appointments stored");
		return result;
	}
	private String printApt(int index, appointment a)
	{
		String result = "";
		result += (index+1)+". On "+a.date+" ";
		result += "at "+a.time+" ";
		result += "with "+a.with;
		return result;
	}
	public void saveList()
	{
		try
		{
			if(!aptList.isEmpty())
			{
				FileOutputStream file_out_strm = new FileOutputStream("appointments");
				ObjectOutputStream objOutStrm = new ObjectOutputStream(file_out_strm);
				
				for(int i = (aptList.size() -1); i >= 0; i--)
				{
					tempStr.add(aptList.get(i).date);
					tempStr.add(aptList.get(i).time);
					tempStr.add(aptList.get(i).with);
				}
				//objOutStrm.writeObject(aptList);
				objOutStrm.writeObject(tempStr);
				
				objOutStrm.close();
				file_out_strm.close();
			}
		}
		catch (FileNotFoundException e)
		{
			System.err.println("File Not found!");
		}
		catch (IOException e)
		{
			System.err.println("Input Output Exception!");
		}
	}
	private void loadList()
	{
		try
		{
			File f = new File("appointments");
			if(f.exists())
			{
				FileInputStream file_in_strm = new FileInputStream("appointments");
				ObjectInputStream objInStrm = new ObjectInputStream(file_in_strm);
				
				//aptList = (ArrayList<appointment>) objInStrm.readObject();
				//aptList = (ArrayList<appointmentList.appointment>) objInStrm.readObject();
				
				tempStr = (ArrayList<String>) objInStrm.readObject();
				
				while(!tempStr.isEmpty())
				{
					String with = tempStr.remove(tempStr.size() -1);
					String time = tempStr.remove(tempStr.size() -1);
					String date = tempStr.remove(tempStr.size() -1);
					appointment newApt = new appointment(date, time, with);
					aptList.add(newApt);
				}

				objInStrm.close();
				file_in_strm.close();
			}
		}
		catch (FileNotFoundException f)
		{
			System.err.println("File Not found!");
		}
		catch (IOException i)
		{
			System.err.println("Input Output Exception!");
		}
		catch (ClassNotFoundException c)
		{
			System.err.println("Class Not Found Exception!");
		}
	}
}



