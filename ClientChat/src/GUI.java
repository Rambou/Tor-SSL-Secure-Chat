/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
//<editor-fold defaultstate="collapsed" desc="Imports">
import java.awt.event.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
//</editor-fold>

/**
 * Form for our Connection/Authentication to the server
 */
public final class GUI extends JFrame {

    //Creation of our swing controls             
    private final JTextArea LogText;
    private final JButton File, Send, Connect;
    private final JScrollPane jScrollPane1;
    private final JTextField txt_Chat, ServerIp;
    private static ClientChat Client;
    private final JRadioButton Plain_Check, SSl_Check;
    private final JCheckBox Tor_Check;
    private final ButtonGroup buttonGroup1;

    GUI() throws UnknownHostException {
        //Frame title
        this.setTitle("Chat Client");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Starting frame at center
        setLocationRelativeTo(null);

        //Initiating Swing controls
        jScrollPane1 = new javax.swing.JScrollPane();
        LogText = new javax.swing.JTextArea();
        txt_Chat = new javax.swing.JTextField();
        Send = new javax.swing.JButton();
        File = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        ServerIp = new javax.swing.JTextField();
        Connect = new javax.swing.JButton();
        Plain_Check = new javax.swing.JRadioButton();
        SSl_Check = new javax.swing.JRadioButton();
        Tor_Check = new javax.swing.JCheckBox();

        //Send button as the default action when pressing the ENTER key
        this.getRootPane().setDefaultButton(Send);

        //Creating action listeners for buttons
        Send.addActionListener((ActionEvent evt) -> {
            btn_Send_Clicked();
        });

        File.addActionListener((ActionEvent evt) -> {
            btn_FileSelect_Clicked();
        });
        Connect.addActionListener((ActionEvent evt) -> {
            btn_Connect_Clicked();
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ServerIp.setText(InetAddress.getLocalHost().getHostAddress());

        Connect.setText("Connect");

        LogText.setColumns(20);
        LogText.setRows(5);
        LogText.setEditable(false);
        jScrollPane1.setViewportView(LogText);

        File.setText("File");

        buttonGroup1.add(Plain_Check);
        Plain_Check.setText("Plain");
        Plain_Check.setSelected(true);
        buttonGroup1.add(SSl_Check);
        SSl_Check.setText("SSL/TLS");

        Send.setText("Send");

        Tor_Check.setText("Through Tor");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(File)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_Chat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Send))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(ServerIp, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Plain_Check)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SSl_Check)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Tor_Check)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Connect))
                .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(ServerIp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Plain_Check)
                                .addComponent(SSl_Check)
                                .addComponent(Connect)
                                .addComponent(Tor_Check))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(File)
                                .addComponent(txt_Chat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Send)))
        );

        pack();
    }

    public static void main(String args[]) {

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }

            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Create and display the form
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new GUI().setVisible(true);
            } catch (Exception ex) {

            }
        });

    }

    public void btn_Send_Clicked() {
        String userInput = txt_Chat.getText();

        //Clear the textbox after getting the message text
        txt_Chat.setText("");

        if (userInput.contains("file:")) {
            if (userInput.substring(0, 5).compareTo("file:") == 0 && userInput.length() >= 4) {
                Client.ExchangeFile(null, userInput.split(":")[1], "ClientReceivesFile");
            } else {
                printMsg(Client.ExchangeMessage(userInput));
            }
        } else {
            printMsg(Client.ExchangeMessage(userInput));
        }
    }

    public void btn_FileSelect_Clicked() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            System.out.println(selectedFile.getPath());
            Client.ExchangeFile(selectedFile.getPath(), selectedFile.getName(), "ClientSendsFile");
        }
    }

    public void printMsg(String msg) {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        msg = "[" + sdf.format(cal.getTime()) + "] " + msg + "\n";
        LogText.append(msg);
        System.out.println(msg);
    }

    public void btn_Connect_Clicked() {
        if (Connect.getText().equals("Connect")) {
            try {
                //<editor-fold defaultstate="collapsed" desc="Get connection type"> 
                ClientChat.Select sl = null;
                if (Plain_Check.isSelected()) {
                    sl = ClientChat.Select.Plain;
                } else if (SSl_Check.isSelected()) {
                    sl = ClientChat.Select.SSl;
                } else {
                    printMsg("Choose a radio button.");
                    return;
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Initialize ClientChat">
                if (Tor_Check.isSelected()) {
                    Client = new ClientChat(this, sl, true, ServerIp.getText());
                } else {
                    Client = new ClientChat(this, sl, false, ServerIp.getText());
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Connect to server">
                if (!Client.Connect()) {
                    return;
                }
                //</editor-fold>

                Connect.setText("Disconnect");
            } catch (Exception ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                printMsg("Failed. Denial. Facepalm etc.");
            }
        } else {
            Client.Disconnect();
            Connect.setText("Connect");
        }

    }
}
