import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;
import java.net.*;  
import java.io.*;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

 
public class fileserver 
{ 
    public static ServerSocket sersocket;
    public static DatagramSocket udp_soc;
    // public static HashMap<String, Socket> userpersocket = new HashMap<String, Socket>();
    // public static HashMap<String, Vector <String> > userpergroup = new HashMap<String, Vector <String> >();
    // public static HashMap<String, Vector <String> > groupperuser = new HashMap<String, Vector <String> >();
    // public static HashMap<String, Vector <String> > userperfiles = new HashMap<String, Vector <String> >();
	public static void main(String[] args) throws IOException, UnknownHostException{ 
		int port = 3060;
        int udpport = 3061;
        sersocket = new ServerSocket(port);
        udp_soc = new DatagramSocket(udpport);
        System.out.println("Server online, waiting for requests");
        System.out.println("Server:>>");
		// running infinite loop for getting 
		// client request 
		while (true) 
		{ 
			Socket soc = null; 
			
			try
			{ 
				soc = sersocket.accept();
                DataInputStream dis = new DataInputStream(soc.getInputStream());
                DataOutputStream dos = new DataOutputStream(soc.getOutputStream());  
				Thread t = new ClientHandler(soc, udp_soc, dis, dos); 

				// Invoking the start() method 
				t.start(); 
				
			} 
			catch (Exception e){ 
				soc.close(); 
				// e.printStackTrace(); 
			} 
		} 
	} 
} 

// ClientHandler class 
class ClientHandler extends Thread{
	final DataInputStream dis; 
	final DataOutputStream dos; 
    final Socket soc; 
    final DatagramSocket udp_soc;
	public static HashMap<String, Socket> userpersocket = new HashMap<String, Socket>();
    public static HashMap<String, Vector <String> > userpergroup = new HashMap<String, Vector <String> >();
    public static HashMap<String, Vector <String> > groupperuser = new HashMap<String, Vector <String> >();
    public static HashMap<String, Vector <String> > userperfiles = new HashMap<String, Vector <String> >();

	// Constructor 
	public ClientHandler(Socket soc,  DatagramSocket udp_soc, DataInputStream dis, DataOutputStream dos){ 
        this.soc = soc; 
        this.udp_soc = udp_soc;
		this.dis = dis; 
		this.dos = dos; 
	} 

	@Override
	public void run(){ 
		String commandRcvd;
		String[] parsedrevcdcmd;
		while (true) { 
			try { 
                commandRcvd = dis.readUTF();
                parsedrevcdcmd = commandRcvd.split(":");
                
                if(parsedrevcdcmd[0].equals("username")){
                    String newuser = parsedrevcdcmd[1];
                    if(!userpersocket.containsKey(newuser)){
                        userpersocket.put(newuser, soc);
                        dos.writeUTF(newuser + " Registered!");
                        System.out.println("user "+ parsedrevcdcmd[1] + " added successfully with ip " + parsedrevcdcmd[2]);
                        System.out.println("Server:>>");
                    }
                    else{
                        dos.writeUTF("Username " + newuser + " already exists!!!");
                        System.out.println("Got request to add multiple users with username " + newuser );
                        System.out.println("Server:>>");
                    }
                }

            // ------------------------------ file upload ----------------------------------//

                else if(parsedrevcdcmd[0].equals("upload")){
                    if(userpersocket.containsKey(parsedrevcdcmd[3])){
                        String username = parsedrevcdcmd[3];
                        File uplfile = new File(parsedrevcdcmd[1]);
                        uplfile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(uplfile);
                        BufferedOutputStream buffoutputstream = new BufferedOutputStream(fos);
                        int read = 0;
                        int filesize = Integer.parseInt(parsedrevcdcmd[2]);
                        int uploadedsize = 0; 
                        byte[] mybytearray = new byte[1024];
                        while((read = dis.read(mybytearray)) > 0){
                            buffoutputstream.write(mybytearray, 0, read);
                            buffoutputstream.flush();
                            uploadedsize += read;
                            if(uploadedsize >= filesize){
                                break;
                            }
                        }
                        buffoutputstream.close();
                        System.out.println(parsedrevcdcmd[1] + " file uploaded successfully by " + parsedrevcdcmd[3]);
                        System.out.println("Server:>>");
                        dos.writeUTF(parsedrevcdcmd[1] + " file is successfully uploaded to server");
                        Vector<String> files = new Vector<String>();
                        if(userperfiles.containsKey(username)){
                            files = userperfiles.get(username);
                        }
                        files.add(parsedrevcdcmd[1]);
                        userperfiles.put(username, files);
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }
            
            // ---------------------- upload_udp file --------------------------------------- //

                else if(parsedrevcdcmd[0].equals("upload_udp")){
                    if(userpersocket.containsKey(parsedrevcdcmd[3])){
                        String username = parsedrevcdcmd[3];
                        File uplfile = new File(parsedrevcdcmd[1]);
                        uplfile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(uplfile);
                        BufferedOutputStream buffoutputstream = new BufferedOutputStream(fos);
                        long uploadedsize = 0;
                        long filesize = Integer.parseInt(parsedrevcdcmd[2]);
                        byte[] mybytearray = new byte[1024];
                        while(uploadedsize < filesize){
                            DatagramPacket uploadedpacket = new DatagramPacket(mybytearray, mybytearray.length);
                            udp_soc.receive(uploadedpacket);
                            buffoutputstream.write(mybytearray, 0, 1024);
                            buffoutputstream.flush();
                            uploadedsize += 1024;
                        }
                        buffoutputstream.close();
                        System.out.println(parsedrevcdcmd[1] + " file uploaded using udp packets successfully by " + parsedrevcdcmd[3]);
                        System.out.println("Server:>>");
                        dos.writeUTF(parsedrevcdcmd[1] + " file is successfully uploaded using udp packets to server");
                        Vector<String> files = new Vector<String>();
                        if(userperfiles.containsKey(username)){
                            files = userperfiles.get(username);
                        }
                        files.add(parsedrevcdcmd[1]);
                        userperfiles.put(username, files);
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }

            // ------------------------- creating folder ------------------------------------ //

                else if(parsedrevcdcmd[0].equals("create_folder")){
                    if(userpersocket.containsKey(parsedrevcdcmd[2])){
                        String dir_name = parsedrevcdcmd[1];
                        File file = new File(dir_name);
                        if(!file.exists()){
                            if (file.mkdir()) {
                                dos.writeUTF("Directory " + dir_name + " created successfully!");
                                System.out.println("Directory " + dir_name + " is created by " + parsedrevcdcmd[2]);
                            }
                            else {
                                dos.writeUTF("Error occured while creating the directory " + dir_name);
                                System.out.println("Failed to create directory!");
                            }   
                        }
                        else{
                            dos.writeUTF("Directory with that name already exists");
                            System.out.println("Directory already exists");
                        }
                    
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }
            
            // -------------------------- move file ------------------------------------------- //

                else if(parsedrevcdcmd[0].equals("move_file")){
                    if(userpersocket.containsKey(parsedrevcdcmd[3])){
                        File afile = new File(parsedrevcdcmd[1]);
                        File bfile = new File(parsedrevcdcmd[2]);
                        FileInputStream fisa = new FileInputStream(afile);
                        FileOutputStream fisb = new FileOutputStream(bfile);
                        byte[] mybytearray = new byte[1024];
                        int length;
                        while ((length = fisa.read(mybytearray)) > 0){
                            fisb.write(mybytearray, 0, length);
                        }
                        fisa.close();
                        fisb.close();
                        afile.delete();
                        dos.writeUTF("File moved successfully");
                        System.out.println("file successfully moved by " + parsedrevcdcmd[3]);
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }

                }

            // ------------------------ create group --------------------------------------- //
                
                else if(parsedrevcdcmd[0].equals("create_group")){
                    if(userpersocket.containsKey(parsedrevcdcmd[2])){
                        String group_name = parsedrevcdcmd[1];
                        String cur_user = parsedrevcdcmd[2];
                        if(userpergroup.containsKey(cur_user)){
                            // String old_group = userpergroup.get(cur_user);
                            dos.writeUTF("You are in another group");
                            System.out.println("User already in a group");
                        }
                        else{
                            dos.writeUTF("You are a new User!");
                            System.out.println("New User!");
                        }
                        if(!groupperuser.containsKey(group_name)){
                            Vector <String> users = new Vector<String>();
                            users.add(cur_user);
                            groupperuser.put(group_name, users);
                            Vector <String> groups = new Vector<String>();
                            if(userpergroup.containsKey(cur_user)){
                                groups = (Vector<String>)userpergroup.get(cur_user);
                            }
                            groups.add(group_name);
                            userpergroup.put(cur_user, groups);
                            dos.writeUTF("Creating the group " + group_name +" and you are the first person in the group");
                            System.out.println("Creating the group " + group_name +" and " + cur_user + " is the first person in the group");
                        }
                        else{
                            dos.writeUTF("Group " + group_name + " already exists");
                            System.out.println("Creating the group " + group_name +" and " + cur_user + " is the first person in the group");
                        }
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        dos.writeUTF("Register before starting to create a group");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }

                // -------------------------- list groups ----------------------------------- //

                else if(parsedrevcdcmd[0].equals("list_groups")){
                    if(userpersocket.containsKey(parsedrevcdcmd[1])){
                        String listinggroups = "";
                        for(String key : groupperuser.keySet()){
                            listinggroups += key + ":";
                        }
                        dos.writeUTF(listinggroups);
                        System.out.println("List of groups requested by " + parsedrevcdcmd[1]);
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        dos.writeUTF("Register before starting to create a group");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }

                // ---------------------------- join group -------------------------------- //

                else if(parsedrevcdcmd[0].equals("join_group")){
                    if(userpersocket.containsKey(parsedrevcdcmd[2])){
                        String groupname = parsedrevcdcmd[1];
                        String username = parsedrevcdcmd[2];
                        if(!groupperuser.containsKey(groupname)){
                            dos.writeUTF("Group doesn't exist!");
                            System.out.println("Group doesn't exist!");
                            System.out.println("Server:>>");
                        }
                        else{
                            if(!userpergroup.containsKey(username)){
                                Vector <String> groups = new Vector <String>();
                                groups.add(groupname);
                                userpergroup.put(username, groups);
                                dos.writeUTF("You joined the group successfully!");
                            }
                            else{
                                Vector<String> groups = new Vector<String>();
                                groups = userpergroup.get(username);
                                if(!groups.contains(groupname)){
                                    groups.add(groupname);
                                    userpergroup.put(username, groups);
                                    dos.writeUTF("You joined the group " + groupname + " successfully!");
                                }
                                else{
                                    dos.writeUTF("You are in the group already");
                                }
                            }
                            Vector <String> users = new Vector<String>();
                            users = groupperuser.get(groupname);
                            if(!users.contains(username)){
                                users.add(username);
                            }
                            groupperuser.put(groupname, users);
                            System.out.println(username + " joined the group " + groupname + " successfully!");
                            System.out.println("Server:>>");
                        }
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        dos.writeUTF("Register before starting to create a group");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }

            // ---------------------------- leave group ----------------------------------- //

                else if(parsedrevcdcmd[0].equals("leave_group")){
                    if(userpersocket.containsKey(parsedrevcdcmd[2])){
                        String groupname = parsedrevcdcmd[1];
                        String username = parsedrevcdcmd[2];
                        if(!groupperuser.containsKey(groupname)){
                            dos.writeUTF("Group doesn't exist!");
                            System.out.println("Group doesn't exist!");
                            System.out.println("Server:>>");
                        }
                        else{
                            // check if he is in group, then remove him from group and remove the group from him
                            Vector <String> users = groupperuser.get(groupname);
                            if(users.contains(username)){
                                users.remove(username);
                                groupperuser.put(groupname, users);
                                Vector <String> groups = userpergroup.get(username);
                                groups.remove(groupname);
                                userpergroup.put(username, groups);
                                dos.writeUTF("You successfully left the group " + groupname);
                                System.out.println(username + " are successfully removed from the group " + groupname);
                            }
                            else{
                                dos.writeUTF("You are not in the requested group");
                                System.out.println(username + " not in the group");
                                System.out.println("Server:>>");
                            }
                        }
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        dos.writeUTF("Register before starting to create a group");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }

                //--------------------------------- list detail ---------------------------------- //

                else if(parsedrevcdcmd[0].equals("list_detail")){
                    if(userpersocket.containsKey(parsedrevcdcmd[2])){
                        String groupname = parsedrevcdcmd[1];
                        String username = parsedrevcdcmd[2];
                        if(!groupperuser.containsKey(groupname)){
                            dos.writeUTF("Group doesn't exist!");
                            System.out.println("Group doesn't exist!");
                            System.out.println("Server:>>");
                        }
                        else{
                            Vector <String> users = groupperuser.get(groupname);
                            if(users.contains(username)){
                                // String listdetails = "";
                                for(String user : users ){
                                    dos.writeUTF("user ==>:" + user);
                                    dos.writeUTF("Following are the files uploaded by the user:");
                                    if(userperfiles.containsKey(user)){
                                        Vector<String> files = userperfiles.get(user);
                                        Iterator value = files.iterator();
                                        while(value.hasNext()){
                                            dos.writeUTF("\t"+value.next());
                                        }
                                    }
                                    else{
                                        dos.writeUTF(" -----------No files uploaded by this user ------------");
                                    }
                                }
                                dos.writeUTF("qwertyuiop");
                                System.out.println("List of users' details for the group " + groupname + " requested by " + username); 
                                System.out.println("Server:>>");
                            }
                            else{
                                dos.writeUTF("You are not in the requested group");
                                dos.writeUTF("qwertyuiop");
                                System.out.println(username + " not in the group");
                                System.out.println("Server:>>");
                            }
                        }
                        
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        dos.writeUTF("Register before starting to create a group");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    } 
                }
            
                // --------------------------- get_file ------------------------------------ //

                else if(parsedrevcdcmd[0].equals("get_file")){
                    String filepath = parsedrevcdcmd[1];
                    String groupname = parsedrevcdcmd[2];
                    String username = parsedrevcdcmd[3];
                    if(userpersocket.containsKey(parsedrevcdcmd[3])){
                        File file = new File(filepath);
                        if(!file.exists()){
                            dos.writeUTF("file not exists");
                        }
                        if(!file.isFile()){
                            dos.writeUTF("Not a file");
                        }
                        if(!groupperuser.containsKey(groupname)){
                            dos.writeUTF("group doesn't exist");
                        }
                        else{
                            Vector<String> users = new Vector<String>();
                            users = groupperuser.get(groupname);
                            boolean flag = false;
                            if(!users.contains(username)){ 
                                dos.writeUTF("you do not belong to the group to acccess files");
                                System.out.in("Denied user to access unauthorised file system");
                            }
                            else{
                                for(String user: users){
                                    Vector<String> files = userperfiles.get(user);
                                    if(files.contains(filepath)){
                                        flag = true;
                                    }
                                }
                                if(flag){
                                    String[] filepar = filepath.split("/");
                                    int l = filepar.length;
                                    String file_name = filepar[l-1];
                                    // dos.writeUTF(file_name);
                                    File filename = new File(file_name);
                                    FileInputStream fileinpstream = new FileInputStream(filename);
                                    BufferedInputStream bis = new BufferedInputStream(fileinpstream);
                                    long size = filename.length(), uploadedsize = 0;
                                    byte[] mybytearray = new byte[(int)size];
                                    dos.writeUTF("download:" + file_name + ":" + String.valueOf(size));
                                    System.out.println("uploading " + file_name + " to client ......");
                                    while(uploadedsize != size){
                                        long window_size = 1024;
                                        if(size - uploadedsize >= window_size){
                                            uploadedsize += window_size;
                                        }
                                        else{
                                            window_size = size - uploadedsize;
                                            uploadedsize = size;
                                        }
                                        mybytearray = new byte[(int)window_size];
                                        bis.read(mybytearray, 0, (int)window_size); 
                                        dos.write(mybytearray);
                                    }
                                    System.out.println("File downloaded by " + username);
                                    System.out.println("Server:>>");
                                }
                                else{
                                    dos.writeUTF("You are not allowed to access the files outside your sharing system");
                                    System.out.println("Unauthorised access to filesystem");
                                    System.out.println("Server:>>");
                                }
                            }      
                        }
                    }
                    else{
                        dos.writeUTF("No user registered from this socket to send requests");
                        dos.writeUTF("Register before starting to create a group");
                        System.out.println("Request came from invalid user...");
                        System.out.println("Server:>>");
                    }
                }
            } 
			catch (IOException e) { 
				// e.printStackTrace(); 
			} 
		 
		
            try{ 
                // closing resources 
                this.dis.close(); 
                this.dos.close(); 
                
            }
            catch(IOException e){ 
                // e.printStackTrace(); 
            } 
        }
    }
}
