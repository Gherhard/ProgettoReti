package Client;

import java.awt.EventQueue;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.event.ActionEvent;


/*The Gui(Graphical user interface) class is divided into many Panels. One frame and many panels.
 * The frame changes its panel depending on the operation a client does. */
public class Gui extends JFrame{

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField usrField;
	private JTextField pswField;
	public int cflag=0;
	SocialClient t = null;
	JTextArea messagelist = new JTextArea();	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 * @throws IOException 
	 */
	//frame constructor
	public Gui() throws IOException {
		this.frame = new JFrame();
		this.frame.setBounds(100, 100, 450, 300);
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.frame.getContentPane().setLayout(null);
		this.frame.setContentPane(loginPanel());
	}

	public void addMessage(String msg){
		if(msg!=null){
			    messagelist.append(msg + "\n");
		}
			
	}
	//The LogIn panel is the start panel. If the log in is successful it goes to the logged panel.
	public JPanel loginPanel() {
		frame.addWindowListener(new WindowAdapter() {
			   public void windowClosing(WindowEvent evt) {
				   int confirm = JOptionPane.showOptionDialog(
				             null, "Are you sure you want to close the application?", 
				             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
				             JOptionPane.QUESTION_MESSAGE, null, null, null);
				        if (confirm == 0) {
				        	System.exit(0);
				        }    
				   }
				  });
		frame.setBounds(100, 100, 450, 300);
		JPanel loginpanel = new JPanel();
		loginpanel.setLayout(null);
		loginpanel.setBounds(0, 0, 450, 278);
		loginpanel.setLayout(null);
		
		JLabel lbl1=new JLabel("Username");
		lbl1.setBounds(152,36,180,30);
		loginpanel.add(lbl1);
		
		JLabel lbl2=new JLabel("Password");
		lbl2.setBounds(152,104,300,30);
		loginpanel.add(lbl2);
		
		usrField = new JTextField();
		usrField.setBounds(144, 66, 130, 26);
		loginpanel.add(usrField);
		usrField.setColumns(10);
		
		pswField = new JPasswordField();
		pswField.setBounds(144, 132, 130, 26);
		loginpanel.add(pswField);
		pswField.setColumns(10);
		
		JButton btnNewButton = new JButton("Log In");
		btnNewButton.setEnabled(false);
		
		Gui gui = this;
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				t = new SocialClient(usrField.getText(),pswField.getText(),gui);
				int temp = t.login();		
				if(temp == -1)
					JOptionPane.showMessageDialog(frame, "Wrong Username or Password!");
				else{
					frame.setContentPane(loggedPanel(usrField.getText()));
					frame.revalidate();
					
				}
	
			}
		});
		btnNewButton.setBounds(63, 205, 117, 29);
		loginpanel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Register");
		btnNewButton_1.setEnabled(false);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				t = new SocialClient(usrField.getText(),pswField.getText(),gui);
				int temp = t.register();
				if(temp==0)
					JOptionPane.showMessageDialog(frame, "User Registered!");
				else
					JOptionPane.showMessageDialog(frame, "ERROR: User was not registered!");
				usrField.setText("");
				pswField.setText("");
				frame.revalidate();
				
			}
		});
		btnNewButton_1.setBounds(242, 205, 117, 29);
		loginpanel.add(btnNewButton_1);
		
		DocumentListener d = new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    changed();
			  }
			  
			  public void insertUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void removeUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void changed() {
				     if ((usrField.getText().isEmpty()) || (pswField.getText().isEmpty())){
				    	 btnNewButton.setEnabled(false);
				    	 btnNewButton_1.setEnabled(false);
				     }
				     else {
				    	 btnNewButton.setEnabled(true);
				    	 btnNewButton_1.setEnabled(true);
				    }

				  }
			
		};
		
		usrField.getDocument().addDocumentListener(d);
		pswField.getDocument().addDocumentListener(d);
		
		return loginpanel;
	}
	//The most important panel. This has most of the operations on it. 
	//More importantly if i quit the app without logging out this panel will do the logout for me.
	//Every operation has a control that brings the frame back to the login panel in case the token expired.
	public JPanel loggedPanel(String arg) {
		frame.addWindowListener(new WindowAdapter() {
			   public void windowClosing(WindowEvent evt) {
				   int confirm = JOptionPane.showOptionDialog(
				             null, "Are you sure you want to close the application?", 
				             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
				             JOptionPane.QUESTION_MESSAGE, null, null, null);
				        if (confirm == 0) {
				        	t.logout();
				        	System.exit(0);
				        }    
				   }
				  });
		frame.setSize(600, 700);
		JPanel loggedpanel = new JPanel();
		loggedpanel.setLayout(null);
		loggedpanel.setSize(500, 500);
		

		JLabel lbl1=new JLabel("Welcome " + arg +"!");
		lbl1.setBounds(200,20,180,30);
		loggedpanel.add(lbl1);
		
		
		JButton btnNewButton = new JButton("Add Friend");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

					frame.setContentPane(addFriendPanel(arg));
					frame.revalidate();
				}
		});
		btnNewButton.setBounds(63, 100, 150, 29);
		loggedpanel.add(btnNewButton);
		
		JButton btn2 = new JButton("Confirm Friends");
		btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
					frame.setContentPane(confirmPanel(arg));
					frame.revalidate();
				}
		});
		btn2.setBounds(63, 130, 150, 29);
		loggedpanel.add(btn2);
		
		JList<String> list = new JList<String>();
		list.setBounds(380,70,179,300);
		loggedpanel.add(list);
		
		JButton btn3 = new JButton("Friend List");
		btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					
				DefaultListModel<String> fl = t.getFriendsList();
				
				if(fl == null)//only if token expired
				{
					frame.setContentPane(loginPanel());
					frame.revalidate();
				}
				else
					list.setModel(fl);
									
			}

		});
		btn3.setBounds(63, 160, 150, 29);
		loggedpanel.add(btn3);
		
		JButton btn4 = new JButton("Find Person");
		btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setContentPane(findPanel(arg));
				frame.revalidate();							
				}
		});
		btn4.setBounds(63, 190, 150, 29);
		loggedpanel.add(btn4);
		
		
		JButton btn5 = new JButton("Follow Person");
		btn5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					frame.setContentPane(FollowPanel(arg));
					frame.revalidate();
				}
		});
		btn5.setBounds(63, 220, 150, 29);
		loggedpanel.add(btn5);
		
		JTextArea textarea = new JTextArea();
		loggedpanel.add(textarea);
		textarea.setLocation(30, 380);
	    textarea.setSize(340, 110);
	    textarea.setEditable(true);

		
		JButton pubb = new JButton("Publish");
		pubb.setEnabled(false);
		pubb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
					
					int res1 = t.publish(textarea.getText());
					if(res1 == -2){
						JOptionPane.showMessageDialog(frame, "ERROR: Token Expired!");
						frame.setContentPane(loginPanel());
						frame.revalidate();
					} else{
						if(res1 == -1){
							JOptionPane.showMessageDialog(frame, "ERROR: Message was not pubblished!");
						}
						else {
							JOptionPane.showMessageDialog(frame, "Message Published!");
							frame.setContentPane(loggedPanel(arg));
							frame.revalidate();
						}
					}
				}
		});
		pubb.setBounds(25, 500, 150, 29);
		loggedpanel.add(pubb);
		textarea.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    changed();
			  }
			  
			  public void insertUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void removeUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void changed() {
				     if (textarea.getText().isEmpty()){
				       pubb.setEnabled(false);
				     }
				     else {
				       pubb.setEnabled(true);
				    }

				  }
			
		});
		
		
		JButton bt6 = new JButton("Read Messages");
		bt6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					frame.setContentPane(msgPanel(arg));
					frame.revalidate();
					
				}
		});
		bt6.setBounds(63, 250, 150, 29);
		loggedpanel.add(bt6);

		JButton outb = new JButton("Log Out");
		outb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
					t.logout();
					frame.setContentPane(loginPanel());
					frame.revalidate();
				}
		});
		outb.setBounds(230, 600, 150, 29);
		loggedpanel.add(outb);	
		return loggedpanel;
	}
	//The Add friend panel.
	public JPanel addFriendPanel(String arg) {
		frame.setSize(300, 210);
		JPanel richpanel = new JPanel();
		richpanel.setLayout(null);
		richpanel.setSize(300,210);

		JLabel lbl1=new JLabel("Insert user name");
		lbl1.setBounds(20,20,180,30);
		richpanel.add(lbl1);
		
		JTextField u = new JTextField();
		u.setBounds(20, 50, 130, 26);
		richpanel.add(u);
		u.setColumns(10);
		
		
		JButton btnNewButton = new JButton("Request Friendship");
		btnNewButton.setEnabled(false);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
					int ris = t.friendRequestC(u.getText());
					if(ris == -2){
						JOptionPane.showMessageDialog(frame, "ERROR: Token Expired!");
						frame.setContentPane(loginPanel());
						frame.revalidate();
					} else{
						if(ris == -1){
							JOptionPane.showMessageDialog(frame, "ERROR: Request was not sent!");
							frame.setContentPane(loggedPanel(arg));
							frame.revalidate();
						}
						else {
							JOptionPane.showMessageDialog(frame, "Request has been sent succesfully!");
							frame.setContentPane(loggedPanel(arg));
							frame.revalidate();
						}
					}
			}
		});
		btnNewButton.setBounds(15, 80, 150, 29);
		richpanel.add(btnNewButton);

		u.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    changed();
			  }
			  
			  public void insertUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void removeUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void changed() {
				     if (u.getText().isEmpty()){
				    	 btnNewButton.setEnabled(false);
				     }
				     else {
				    	 btnNewButton.setEnabled(true);
				    }

				  }
			
		});
		return richpanel;
	}
	//The confirm friend requests panel.
	public JPanel confirmPanel(String arg) {
		frame.setSize(500, 500);
		JPanel cpanel = new JPanel();
		cpanel.setLayout(null);
		cpanel.setSize(500, 500);
		JList<String> list = new JList<String>();
		list.setBounds(250,50,179,300);
		cpanel.add(list);
		
		
		JButton lb = new JButton("Load Friends");
		lb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				DefaultListModel<String> fl = t.getSuspendedList();
				if(fl == null) //because of token expired
				{
					frame.setContentPane(loginPanel());
					frame.revalidate();
				}
				else
					list.setModel(fl);
								
			}
		});
		lb.setBounds(30, 50, 160, 29);
		cpanel.add(lb);
		
        
		JButton b1 = new JButton("Close");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				frame.setContentPane(loggedPanel(arg));
				frame.revalidate();
				
			}
		});
		b1.setBounds(200, 400, 100, 29);
		cpanel.add(b1);
		
		JButton cf = new JButton("Confirm Friend");
		cf.setEnabled(false); //Disable here
		cf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cf.setEnabled(false);
				int ris = t.confirmRequest(list.getSelectedValue());
				if(ris == -2){
					JOptionPane.showMessageDialog(frame, "ERROR: Token Expired!");
					frame.setContentPane(loginPanel());
					frame.revalidate();
				} else{
						JOptionPane.showMessageDialog(frame, "Friend accepted!");
						frame.setContentPane(loggedPanel(arg));
						frame.revalidate();
				}
				DefaultListModel<String> fl = t.getSuspendedList();
				if(fl == null)
				{
					frame.setContentPane(loginPanel());
					frame.revalidate();
				}
				list.setModel(fl);
			}
		});
		
		list.addListSelectionListener(new ListSelectionListener() {
		     public void valueChanged(ListSelectionEvent e) {
		           cf.setEnabled(true);//Enable here
		 }
		});
		cf.setBounds(30, 80, 160, 29);
		cpanel.add(cf);
		
		
		JButton cf1 = new JButton("Delete Request");
		cf1.setEnabled(false); //Disable here
		cf1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cf1.setEnabled(false);
				int ris = t.deleteRequest(list.getSelectedValue());
				if(ris == -2){
					JOptionPane.showMessageDialog(frame, "ERROR: Token Expired!");
					frame.setContentPane(loginPanel());
					frame.revalidate();
				} else{
						JOptionPane.showMessageDialog(frame, "Request deleted!");
						frame.setContentPane(loggedPanel(arg));
						frame.revalidate();
				}
				DefaultListModel<String> fl = t.getSuspendedList();
				if(fl == null)
				{
					frame.setContentPane(loginPanel());
					frame.revalidate();
				}
				list.setModel(fl);
				
	
			}
		});
		
		list.addListSelectionListener(new ListSelectionListener() {
		     public void valueChanged(ListSelectionEvent e) {
		           cf1.setEnabled(true);//Enable here
		 }
		});
		cf1.setBounds(30, 110, 160, 29);
		cpanel.add(cf1);
		return cpanel;
	}
	

	//The find person panel
	public JPanel findPanel(String arg) {
		frame.setSize(500, 500);
		JPanel cpanel = new JPanel();
		cpanel.setLayout(null);
		cpanel.setSize(500, 500);
		JList<String> list = new JList<String>();
		list.setBounds(250,50,179,300);
		cpanel.add(list);
		
		
		JLabel lbl1=new JLabel("Username");
		lbl1.setBounds(35,26,180,30);
		cpanel.add(lbl1);
		
		JTextField u = new JTextField();
		u.setBounds(30, 50, 150, 26);
		cpanel.add(u);
		u.setColumns(10);
		
		
		
		JButton lbc= new JButton("Find");
		lbc.setEnabled(false);
		lbc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				DefaultListModel<String> fl = t.searchUser(u.getText());
				
				if(fl == null)//only if token expired
				{
					frame.setContentPane(loginPanel());
					frame.revalidate();
				}
				else
					list.setModel(fl);

			}
		});
		lbc.setBounds(25, 80, 180, 29);
		cpanel.add(lbc);
		
		u.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    changed();
			  }
			  
			  public void insertUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void removeUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void changed() {
				     if (u.getText().isEmpty()){
				       lbc.setEnabled(false);
				     }
				     else {
				       lbc.setEnabled(true);
				    }

				  }
			
		});
        
		JButton b1 = new JButton("Close");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				frame.setContentPane(loggedPanel(arg));
				frame.revalidate();
				
			}
		});
		b1.setBounds(200, 400, 100, 29);
		cpanel.add(b1);
				
		return cpanel;
	}
	
	//The follow a person panel
	public JPanel FollowPanel(String arg) {
		frame.setSize(300, 150);
		JPanel fpanel = new JPanel();
		fpanel.setLayout(null);
		fpanel.setSize(300, 300);
		
		
		JLabel lbl1=new JLabel("Username");
		lbl1.setBounds(35,15,180,30);
		fpanel.add(lbl1);
		
		JTextField u = new JTextField();
		u.setBounds(30, 35, 150, 26);
		fpanel.add(u);
		u.setColumns(10);
		
		
		
		JButton lbc= new JButton("Follow");
		lbc.setEnabled(false);
		lbc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int res1 = t.subscribe(u.getText());
				if(res1 == -2){
					JOptionPane.showMessageDialog(frame, "ERROR: Token Expired!");
					frame.setContentPane(loginPanel());
					frame.revalidate();
				} else{
					if(res1 == -1){
						JOptionPane.showMessageDialog(frame, "ERROR: Subscription Failed!");
					}
					else {
						JOptionPane.showMessageDialog(frame, "Subscription was successful!");
						frame.setContentPane(loggedPanel(arg));
						frame.revalidate();
					}
				}
				
			}
		});
		lbc.setBounds(25, 60, 180, 29);
		fpanel.add(lbc);
		
		u.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    changed();
			  }
			  
			  public void insertUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void removeUpdate(DocumentEvent e) {
				changed();
				
			  }
			  public void changed() {
				     if (u.getText().isEmpty()){
				       lbc.setEnabled(false);
				     }
				     else {
				       lbc.setEnabled(true);
				    }

				  }
			
		});
        
		JButton b1 = new JButton("Close");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				frame.setContentPane(loggedPanel(arg));
				frame.revalidate();
				
			}
		});
		b1.setBounds(100, 90, 100, 29);
		fpanel.add(b1);
				
		return fpanel;
	}
	
	//The read messages panel.
	public JPanel msgPanel(String arg) {
		frame.setSize(500, 500);
		JPanel fpanel = new JPanel();
		fpanel.setLayout(null);
		fpanel.setSize(300, 300);
		
		JLabel lbl1=new JLabel("Hey "+arg+" this are your unread messagges!");
		lbl1.setBounds(35,15,280,30);
		fpanel.add(lbl1);
		readMessagesFromFile(arg);
		messagelist.setBounds(30,50,340,350);
		fpanel.add(messagelist);
	
		JButton b1 = new JButton("Close");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messagelist.setText("");
				frame.setContentPane(loggedPanel(arg));
				frame.revalidate();
				
			}
		});
		b1.setBounds(200, 420, 100, 29);
		fpanel.add(b1);
				
		return fpanel;
	}

	private void readMessagesFromFile(String usr) {
		
		String fileName = "Messages_"+usr;
		try {
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			fileReader.close();
			messagelist.append(stringBuffer.toString());
			//clean the file
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
		} catch (IOException e) {
			System.err.println("No messages to read!");
		}
		
		
	}
		
}
