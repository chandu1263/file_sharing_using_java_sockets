import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.*;
import java.io.*;

public class fileclient{
    public static void main(String[] args) throws UnknownHostException, IOException{
      String cmd;
      int server_port, udpport;
      String server_ip;
      InetAddress localhost = InetAddress.getLocalHost();
      String currentuser = "randomusernamewhichwontbeused";
      server_ip = "localhost";
      server_port = 3060;
      udpport = 3061;
      while(true){
        BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
        System.out.printf("Client:>>");
        try{
          cmd = buffer.readLine();
          String[] cmds = cmd.split(" ");
          
          // -------------------- exiting from worspace ------------------------- //
          
          if(cmds.length == 1  && cmds[0].equals("exit")){
            System.out.println("Exiting from client's workspace!");
            return;
          }
          Socket clientsocket = new Socket(server_ip, server_port);
          DataInputStream dis = new DataInputStream(clientsocket.getInputStream());
          DataOutputStream dos = new DataOutputStream(clientsocket.getOutputStream());
          
          // ----------------------- create_user ----------------------------------//
          
          if(cmds[0].equals("create_user") && cmds.length == 2){
            try{
              dos.writeUTF("username:" + cmds[1] + ":" + localhost.getHostAddress().trim());
              currentuser = cmds[1];
              String recvdMsg = dis.readUTF();
              System.out.println(recvdMsg);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          //-------------------------- file upload -------------------------------- //

          else if(cmds[0].equals("upload") && cmds.length == 2){
            try{
              File filename = new File(cmds[1]);
              FileInputStream fileinpstream = new FileInputStream(filename);
              BufferedInputStream bis = new BufferedInputStream(fileinpstream);
              long size = filename.length(), uploadedsize = 0;
              byte[] mybytearray = new byte[(int)size];
              dos.writeUTF("upload:" + cmds[1] + ":" + String.valueOf(size) + ":" + currentuser);
              System.out.println("uploading " + cmds[1] + " ......");
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
              String recvdmsg = dis.readUTF();
              System.out.println(recvdmsg);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }
          // ---------------------- udp_file upload --------------------------- //
          
          else if(cmds[0].equals("upload_udp") && cmds.length == 2){
            try{
              File filename = new File(cmds[1]);
              FileInputStream fileinpstream = new FileInputStream(filename);
              BufferedInputStream bis = new BufferedInputStream(fileinpstream);
              long size = filename.length(), uploadedsize = 0;
              byte[] mybytearray = new byte[1024];
              dos.writeUTF("upload_udp:" + cmds[1] + ":" + String.valueOf(size) + ":" + currentuser);
              DatagramSocket udpsoc = new DatagramSocket();
              InetAddress ipaddress = InetAddress.getByName("localhost");
              while(uploadedsize < size){
                int window_size = 1024;
                if(size - uploadedsize >= window_size){
                    uploadedsize += window_size;
                }
                else{
                  window_size = (int)(size-uploadedsize);
                  uploadedsize = size;  
                }
                mybytearray = new byte[window_size];  
                bis.read(mybytearray,0,(int)window_size);
                DatagramPacket udppacket = new DatagramPacket(mybytearray, window_size, ipaddress, udpport);
                udpsoc.send(udppacket);
              }
              udpsoc.close();
              String recvdmsgudp = dis.readUTF();
              System.out.println(recvdmsgudp);
            }
            catch(IOException e){
              e.printStackTrace();
            }   
          }

          // ------------------------ create a folder -------------------------- //
          
          else if(cmds[0].equals("create_folder") && cmds.length == 2){
            try{
              dos.writeUTF(cmds[0] + ":" + cmds[1] + ":" + currentuser);
              String folderrcvdmsg = dis.readUTF();
              System.out.println(folderrcvdmsg);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ------------------------- move file src to dst --------------------- //

          else if(cmds[0].equals("move_file") && cmds.length == 3){
            try{
              dos.writeUTF(cmds[0] + ":" + cmds[1] + ":" + cmds[2] + ":" + currentuser);
              String movefilercvdmsg = dis.readUTF();
              System.out.println(movefilercvdmsg);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ------------------------ Create Group ---------------------------- //

          else if(cmds[0].equals("create_group") && cmds.length == 2){
            try{
              dos.writeUTF(cmds[0] + ":" + cmds[1] + ":" + currentuser);
              String crtgrprcvdmsg1 = dis.readUTF();
              String crtgrprcvdmsg2 = dis.readUTF();
              System.out.println(crtgrprcvdmsg1);
              System.out.println(crtgrprcvdmsg2);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ----------------------- list groups ---------------------------------//

          else if(cmds[0].equals("list_groups") && cmds.length ==1){
            try{
              dos.writeUTF(cmds[0] + ":" + currentuser);
              String listofgroups = dis.readUTF();
              String[] listgroups = listofgroups.split(":");
              for(int i=0;i<listgroups.length;i++){
                System.out.println(listgroups[i]);
              }
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ------------------------ join group --------------------------------- //

          else if(cmds[0].equals("join_group") && cmds.length == 2){
            try{
              dos.writeUTF(cmds[0] + ":" + cmds[1] + ":" + currentuser);
              String joingroupmsgrcvd = dis.readUTF();
              System.out.println(joingroupmsgrcvd);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // -------------------------- leave group ------------------------------ //

          else if(cmds[0].equals("leave_group") && cmds.length == 2){
            try{
              dos.writeUTF(cmds[0] + ":" + cmds[1] + ":" + currentuser);
              String leavegroupmsgrcvd = dis.readUTF();
              System.out.println(leavegroupmsgrcvd);
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ------------------------ list_details ------------------------------ //

          else if(cmds[0].equals("list_detail") && cmds.length == 2){
            try{
              dos.writeUTF(cmds[0]+":"+cmds[1]+":"+currentuser);
              String listdetailsmsgrcvd = "";
              while(true){
                listdetailsmsgrcvd = dis.readUTF();
                if(listdetailsmsgrcvd.equals("qwertyuiop")){
                  break;
                }
                System.out.println(listdetailsmsgrcvd);
              }
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ---------------------------- get file --------------------------------- //

          else if(cmds[0].equals("get_file") && cmds.length == 3){
            try{
              dos.writeUTF(cmds[0]+":"+cmds[1]+":"+cmds[2]+":"+currentuser);
              String getfilemsgrcvd = dis.readUTF();
              String[] getfilemsgrcvdpar = getfilemsgrcvd.split(":");
              if(!getfilemsgrcvdpar[0].equals("download")){
                System.out.println(getfilemsgrcvd);
              }
              else{ 
                String file = getfilemsgrcvdpar[1];
                int filesize = Integer.parseInt(getfilemsgrcvdpar[2]);
                File dwnlfile = new File(file);
                dwnlfile.createNewFile();
                FileOutputStream fos = new FileOutputStream(dwnlfile);
                BufferedOutputStream buffoutputstream = new BufferedOutputStream(fos);
                int read = 0;
                // int filesize = Integer.parseInt(parsedrevcdcmd[2]);
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
                System.out.println("File downloaded successfully");
                fos.close();
                buffoutputstream.close();
              }
            }
            catch(IOException e){
              e.printStackTrace();
            }
          }

          // ------------------------ incorrect command ------------------------ //
          
          else{
            dos.writeUTF("unknown_command");
            System.out.println("UNKNOWN COMMAND!!");
          }
          
          dos.close();
          dis.close();
          clientsocket.close();
        } 
        catch(IOException error){
          System.out.println("*****ERROR: IN READING COMMAND!!!*****");
        }
      }
  }
}

