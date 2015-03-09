import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

/**
 * @author Andre Correia, 41626
 * @author Henrique Garces, 42077
 */

public class Objectos3D implements GLEventListener, KeyListener{

	private static final int FRONT_VIEW = 1;
	private static final int LEFT_VIEW = 2;
	private static final int TOP_VIEW = 3;
	private static final int AXONOMETRIC = 4;
	private static final int OBLIQUE = 5;
	private static final int PERSPECTIVE = 6;
	private static final int RIGHT_VIEW = 7;
	
	private static final int DEFAULT_A = 42;
	private static final int DEFAULT_B = 7;
	private static final int DEFAULT_ALPHA = 45;
	private static final float DEFAULT_L = 1.0f;
	private static final int DEFAULT_D = 1;
	

	static GLCanvas canvas;
	private static ObjectLoader obj;
	private static ObjectTexture objTexture;

	private double width;
	private double height;

	private static ObliqueProjection oProjection;
	private static PerspectiveProjection pProjection;

	private static boolean alcadoPrincipal = true;
	private static boolean alcadoEsquerdo = true;
	private static boolean planta = true;
	private static boolean projeccaoAdicional = true;
	private static boolean escape = false;
	private static boolean init = true;

	private static int fourthQuadrant;
	private static float zoom;
	private static int theta;
	private static int gamma;
	private static boolean wireframeMode;
	private static boolean solidMode;
	private static boolean textureMode;
	private static boolean solidWireframeMode;
	private static boolean hasObject;

	private static JSlider sliderAlphaObliqua;
	private static JSlider sliderLObliqua;
	private static JSlider sliderGammaAxonometrica;
	private static JSlider sliderThetaAxonometrica;
	private static JSlider sliderDPerspectiva;

	private static JRadioButton apRadioButton;
	private static JRadioButton pRadioButton;
	private static JRadioButton aeRadioButton;
	private static JRadioButton adRadioButton;
	
	private static JTabbedPane tabbedPane;
	private static JCheckBoxMenuItem jmenuTexture;
	private static ButtonGroup group;
	private static JRadioButton jmenuWireframe;
	private static int[] texture;
	private final Set<Integer> keyPressed = new HashSet<Integer>();

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();

		double aRatio ;	
		aRatio = width / (double)height; 
		if (width <= height)
			gl.glOrtho (-1.0*zoom, 1.0*zoom, (-1.0/aRatio)*zoom, (1.0/aRatio)*zoom, -2.0, 2.0);
		else
			gl.glOrtho ((-1.0*aRatio)*zoom, (1.0*aRatio)*zoom, -1.0*zoom, 1.0*zoom, -2.0, 2.0);

		//bottom left: top view
		if (planta) {
			if (init || escape)
				gl.glViewport(0, 0, (int)this.width/2, (int)this.height/2);
			else
				gl.glViewport(0, 0, (int)this.width, (int)this.height);
			displayView(gl, TOP_VIEW);
		}

		//top left: front view
		if (alcadoPrincipal) {
			if (init || escape)
				gl.glViewport(0, (int)this.height/2, (int)this.width/2, (int)this.height/2);
			else
				gl.glViewport(0, 0, (int)this.width, (int)this.height);
			displayView(gl, FRONT_VIEW);
		}

		//bottom right
		if (projeccaoAdicional) {
			if (init || escape)
				gl.glViewport((int)this.width/2, 0, (int)this.width/2, (int)this.height/2);
			else
				gl.glViewport(0, 0, (int)this.width, (int)this.height);
			displayView(gl, fourthQuadrant);
		}

		//top right: left view
		if (alcadoEsquerdo) {
			if (init || escape)
				gl.glViewport((int) this.width/2, (int)this.height/2, (int)this.width/2, (int)this.height/2);
			else
				gl.glViewport(0, 0, (int)this.width, (int)this.height);
			displayView(gl, LEFT_VIEW);
		}

		gl.glFlush() ;
	}

	private void displayView(GL2 gl, int view) {
		if(hasObject) {
			gl.glMatrixMode(GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glDisable(GL_TEXTURE_2D);

			switch(view) {
			case 1: break;
			case 2: gl.glRotatef(90, 0, 1, 0); 
				break;
			case 3: gl.glRotatef(90, 1, 0, 0); 
				break;
			case 4: gl.glRotatef(sliderGammaAxonometrica.getValue(), 1, 0, 0); 
					gl.glRotatef(sliderThetaAxonometrica.getValue(), 0, 1, 0); 
					break;
			case 5: gl.glMultMatrixd(oProjection.getMatrix(), 0); 
				break;
			case 6: gl.glDisable(GL_CULL_FACE);
				gl.glMultMatrixd(pProjection.getMatrix(), 0); 
				break;
			case 7: gl.glRotatef(90, 0, -1, 0); 
				break;
			}

			float scaleFactor = obj.getScaleFactor();
			float[] center = obj.getCenter();

			drawGridFloor(gl);

			if(textureMode && solidMode) {
				loadTexture(gl);
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glBindTexture(GL_TEXTURE_2D, texture[0]);
			}

			gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
			gl.glTranslatef(-center[0], -center[1], -center[2]);

			if(solidMode) {
				gl.glColor3d(0.75, 0.75, 0.75);
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
				obj.draw(gl);
			}
			else if(wireframeMode) {
				gl.glColor3d(1.0, 1.0, 1.0);
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
				gl.glEnable( GL2.GL_POLYGON_OFFSET_LINE );
				gl.glPolygonOffset( -1.f, -1.f );
				obj.draw(gl);
				gl.glDisable( GL2.GL_POLYGON_OFFSET_LINE );
			}
			else if(solidWireframeMode) {
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
				gl.glColor3d(0.0, 0.0, 0.0);
				gl.glEnable( GL_POLYGON_OFFSET_FILL );
				gl.glPolygonOffset( 1.f, 1.f );
				obj.draw(gl);
				gl.glDisable( GL_POLYGON_OFFSET_FILL );
				
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE); 
				gl.glColor3d(1.0, 1.0, 1.0);
				obj.draw(gl);
			}
		}
		gl.glEnable(GL_CULL_FACE);
	}

	private void drawGridFloor(GL2 gl) {
		gl.glBegin(GL_LINES);
		for (float i = -1.0f; i <= 1.0; i += 0.25) {
			gl.glColor3d(0.0, 0.30, 0.0);
			gl.glVertex3f(i, -0.5f, 1.0f); 
			gl.glVertex3f(i, -0.5f, -1.0f);
			gl.glVertex3f(1.0f, -0.5f, i); 
			gl.glVertex3f(-1.0f, -0.5f, i);
		}
		gl.glEnd();
	}

	private void loadTexture(GL2 gl) {
		gl.glGenTextures(1, texture, 0);
		gl.glBindTexture(GL_TEXTURE_2D, texture[0]);

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, objTexture.width, objTexture.height,
				0, GL2.GL_BGR, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(objTexture.getTextureBuffer()));

	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		
		GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc( GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_CULL_FACE);
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		sliderAlphaObliqua.addKeyListener(this);
		sliderLObliqua.addKeyListener(this);
		sliderGammaAxonometrica.addKeyListener(this);
		sliderThetaAxonometrica.addKeyListener(this);
		sliderDPerspectiva.addKeyListener(this);
		
		reset();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		this.width = w; this.height = h;

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Projeccoes");
				Objectos3D obj = new Objectos3D();
				canvas = new GLCanvas();
				canvas.addGLEventListener(obj);
				canvas.addKeyListener(obj);
				canvas.setSize(800,500);
				frame.add(canvas);
				frame.pack();
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});
				frame.setVisible(true);
				frame.add(getTabbedMenu(), BorderLayout.SOUTH);
				setupUI(frame);
				canvas.requestFocus();
			}
		});

	}

	private static void reset() {
		obj = new ObjectLoader();
		hasObject = false;
		zoom = 1.0f;
		wireframeMode = true;
		solidMode = false;
		solidWireframeMode = false;
		jmenuWireframe.setSelected(true);
		if(textureMode) {
			textureMode = false;
			jmenuTexture.setSelected(false);
		}
		jmenuTexture.setEnabled(false);

		fourthQuadrant = OBLIQUE;
		oProjection = new ObliqueProjection(DEFAULT_ALPHA, DEFAULT_L);
		pProjection = new PerspectiveProjection(DEFAULT_D);

		alcadoPrincipal = true;
		alcadoEsquerdo = true;
		planta = true;
		projeccaoAdicional = true;
		init = false;
		escape = true;
		
		sliderThetaAxonometrica.setValue(theta);
		sliderGammaAxonometrica.setValue(gamma);
		sliderAlphaObliqua.setValue(DEFAULT_ALPHA);
		sliderLObliqua.setValue((int)DEFAULT_L*10);
		sliderDPerspectiva.setValue(DEFAULT_D);
		tabbedPane.setSelectedIndex(0);
		group.clearSelection();
	}

	private static void setupUI(JFrame frame) {
		
		// MENUBAR DE CIMA
		JMenuBar jmenuBarNORTH = new JMenuBar();
		//adicionar a bar 
		JMenu jmenuFile = new JMenu("File");
		JMenu jmenuView = new JMenu("View");
		JMenu jmenuHelp = new JMenu("Help");
		//adicionar a bar
		jmenuBarNORTH.add(jmenuFile);
		jmenuBarNORTH.add(jmenuView);
		jmenuBarNORTH.add(jmenuHelp);
		// adicionar ao jmenuFile
		JMenuItem jmenuImportModel = new JMenuItem("Import Model");
		JMenuItem jmenuImportTexture = new JMenuItem("Import Texture");
		JMenuItem jmenuReset = new JMenuItem("Reset");
		jmenuFile.add(jmenuImportModel);
		jmenuFile.add(jmenuImportTexture);
		jmenuFile.add(jmenuReset);

		//jmenuView
		ButtonGroup group = new ButtonGroup();
		JRadioButton jmenuSolid = new JRadioButton("Solid Fill");
		jmenuWireframe = new JRadioButton("Wireframe");
		JRadioButton jmenuSolidWireframe = new JRadioButton("Solid & Wireframe");
		jmenuTexture = new JCheckBoxMenuItem("Texture");
		group.add(jmenuSolid);
		group.add(jmenuWireframe);
		group.add(jmenuSolidWireframe);
		jmenuTexture.setEnabled(false);
		jmenuView.add(jmenuWireframe);
		jmenuView.add(jmenuSolid);
		jmenuView.add(jmenuSolidWireframe);
		jmenuView.add(jmenuTexture);
		
		//jmenuHelp
		JMenuItem jmenuShortcuts = new JMenuItem("Shortcuts");
		JMenuItem jmenuAbout = new JMenuItem("About");
		jmenuHelp.add(jmenuShortcuts);
		jmenuHelp.add(jmenuAbout);

		frame.add(jmenuBarNORTH,BorderLayout.NORTH);

		// IMPORT MODEL
		jmenuImportModel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("OBJ type - *.obj","obj");
				chooser.setFileFilter(filter);
				int returnValue = chooser.showOpenDialog(chooser);	
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					try {
						File selectedFile = chooser.getSelectedFile();
						reset();
						hasObject = true;
						obj.load(selectedFile);
						canvas.display();
					} catch (IOException e1) {
						System.out.println(e1.getMessage());
					}
				}
			}

		});

		// IMPORT Texture
		jmenuImportTexture.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Image type - *.png, *.jpg","png", 
						"jpg");
				chooser.setFileFilter(filter);
				int returnValue = chooser.showOpenDialog(chooser);	
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = chooser.getSelectedFile();
					objTexture = new ObjectTexture(selectedFile);
					texture = new int[1];
					System.err.println("Texture loaded: "+selectedFile.getAbsolutePath());
					if(solidMode)
						jmenuTexture.setEnabled(true);
				}
			}
		});

		//Reset
		jmenuReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(event.getActionCommand().equals("Reset")) {
					objTexture = null;
					texture = new int[1];
					reset();
					System.err.println("Resetting... done.");
				}
			}
		});

		class RadioButtonActionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent event) {
				JRadioButton button = (JRadioButton) event.getSource();
				if(button == jmenuWireframe) {
					wireframeMode = true;
					solidMode = false;
					solidWireframeMode = false;
					if(objTexture != null)
						jmenuTexture.setEnabled(false);
				}
				else if(button == jmenuSolid) {
					solidMode = true;
					wireframeMode = false;
					solidWireframeMode = false;
					if(objTexture != null)
						jmenuTexture.setEnabled(true);
				}
				else if(button == jmenuSolidWireframe) {
					solidWireframeMode = true;
					solidMode = false;
					wireframeMode = false;
					if(objTexture != null)
						jmenuTexture.setEnabled(false);
				}
				canvas.repaint();
			}
		}
		RadioButtonActionListener viewModeListener = new RadioButtonActionListener();
		//WIREFRAME
		jmenuWireframe.addActionListener(viewModeListener);

		//SOLID
		jmenuSolid.addActionListener(viewModeListener);

		//SOLID_WIREFRAME
		jmenuSolidWireframe.addActionListener(viewModeListener);

		//TEXTURE
		jmenuTexture.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				AbstractButton button = (AbstractButton) event.getSource();
				boolean selected = button.getModel().isSelected();
				if(selected)
					textureMode = true;
				else
					textureMode = false;
			}
		});

		// SHORTCUTS MESSAGE
		jmenuShortcuts.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Shortcuts \n"
						+ "Ctrl+1 - Menu Projeccao Ortogonal \n"
						+ "Ctrl+2 - Menu Projeccao Obliqua\n"
						+ "Ctrl+3 - Menu Projeccao Axonometrica\n"
						+ "Ctrl+4 - Menu Projeccao Perspectiva\n"
						+ "- - - - - - -\n"
						+ "Menu Projeccao Ortogonal \n"
						+ "A - Alcado Esquerdo\n"
						+ "D - Alcado Direito\n"
						+ "W - Alcado Principal\n"
						+ "S - Planta\n"
						+ "- - - - - - -\n"
						+ "Menu Projeccao Obliqua\n"
						+ "A+LEFT/RIGHT - Parametro alpha\n"
						+ "L+LEFT/RIGHT - Parametro l\n"
						+ "- - - - - - -\n"
						+ "Menu Projeccao Axonometrica\n"
						+ "T+LEFT/RIGHT - Parametro theta\n"
						+ "G+LEFT/RIGHT - Parametro gamma\n"
						+ "- - - - - - -\n"
						+ "Menu Projeccao Perspectiva\n"
						+ "D+LEFT/RIGHT - Parametro d\n"
						+ "- - - - - - -\n"
						+ "'+' - Zoom in\n"
						+ "'-' - Zoom out\n"
						+ "1/2/3/4 - Projeccao unica\n"
						+ "Esc - Projeccoes multiplas");	
			}	
		});
		
		jmenuAbout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "CGI - TB2"
						+ "Andre Correia, 41626\n"
						+ "Henrique Garces, 42077\n"
						+ "P7"); 
				
			}
			
		}) ;
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_1) {
			alcadoPrincipal = true;
			alcadoEsquerdo = false;
			planta = false;
			projeccaoAdicional = false;
			init = false;
			escape = false;
		}
		if (e.getKeyChar() == KeyEvent.VK_2) {
			alcadoPrincipal = false;
			alcadoEsquerdo = true;
			planta = false;
			projeccaoAdicional = false;
			init = false;
			escape = false;
		}

		if (e.getKeyChar() == KeyEvent.VK_3) {
			alcadoPrincipal = false;
			alcadoEsquerdo = false;
			planta = true;
			projeccaoAdicional = false;
			init = false;
			escape = false;
		}

		if (e.getKeyChar() == KeyEvent.VK_4) {
			alcadoPrincipal = false;
			alcadoEsquerdo = false;
			planta = false;
			projeccaoAdicional = true;
			init = false;
			escape = false;
		}
		if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			alcadoPrincipal = true;
			alcadoEsquerdo = true;
			planta = true;
			projeccaoAdicional = true;
			init = false;
			escape = true;
		}
		if(e.getKeyChar()== '+'){
			if(zoom > 0.6) {
				zoom -= 0.05f;
			}
		}
		if(e.getKeyChar()=='-'){
			if(zoom < 1.4) {
				zoom += 0.05f;
			}
		}
		canvas.repaint();
	}

	public void keyPressed(KeyEvent e) {
		keyPressed.add(e.getKeyCode());
		
		if(tabbedPane.getSelectedIndex() == 0) {
			if(e.getKeyCode() == KeyEvent.VK_W)
				apRadioButton.setSelected(true);
			if(e.getKeyCode() == KeyEvent.VK_A)
				aeRadioButton.setSelected(true);
			if(e.getKeyCode() == KeyEvent.VK_D)
				adRadioButton.setSelected(true);
			if(e.getKeyCode() == KeyEvent.VK_S)
				pRadioButton.setSelected(true);
		}
		
		if(keyPressed.size() > 1) {
			switch(tabbedPane.getSelectedIndex()) {	
			case 1:
				if(keyPressed.contains(KeyEvent.VK_A) && keyPressed.contains(KeyEvent.VK_RIGHT))
					sliderAlphaObliqua.setValue(sliderAlphaObliqua.getValue()+1);
				if(keyPressed.contains(KeyEvent.VK_A) && keyPressed.contains(KeyEvent.VK_LEFT))
					sliderAlphaObliqua.setValue(sliderAlphaObliqua.getValue()-1);
				if(keyPressed.contains(KeyEvent.VK_L) && keyPressed.contains(KeyEvent.VK_RIGHT))
					sliderLObliqua.setValue(sliderLObliqua.getValue()+1);
				if(keyPressed.contains(KeyEvent.VK_L) && keyPressed.contains(KeyEvent.VK_LEFT))
					sliderLObliqua.setValue(sliderLObliqua.getValue()-1);
				break;
			
			case 2: 
				if(keyPressed.contains(KeyEvent.VK_T) && keyPressed.contains(KeyEvent.VK_RIGHT))
					sliderThetaAxonometrica.setValue(sliderThetaAxonometrica.getValue()+1);
				if(keyPressed.contains(KeyEvent.VK_T) && keyPressed.contains(KeyEvent.VK_LEFT))
					sliderThetaAxonometrica.setValue(sliderThetaAxonometrica.getValue()-1);
				if(keyPressed.contains(KeyEvent.VK_G) && keyPressed.contains(KeyEvent.VK_RIGHT))
					sliderGammaAxonometrica.setValue(sliderGammaAxonometrica.getValue()+1);
				if(keyPressed.contains(KeyEvent.VK_G) && keyPressed.contains(KeyEvent.VK_LEFT))
					sliderGammaAxonometrica.setValue(sliderGammaAxonometrica.getValue()-1);
				break;
			
			case 3: 
				if(keyPressed.contains(KeyEvent.VK_D) && keyPressed.contains(KeyEvent.VK_RIGHT))
					sliderDPerspectiva.setValue(sliderDPerspectiva.getValue()+1);
				if(keyPressed.contains(KeyEvent.VK_D) && keyPressed.contains(KeyEvent.VK_LEFT))
					sliderDPerspectiva.setValue(sliderDPerspectiva.getValue()-1);
				break;
			}
			
			if(keyPressed.contains(KeyEvent.VK_CONTROL) && keyPressed.contains(KeyEvent.VK_1))
				tabbedPane.setSelectedIndex(0);
			if(keyPressed.contains(KeyEvent.VK_CONTROL) && keyPressed.contains(KeyEvent.VK_2))
				tabbedPane.setSelectedIndex(1);
			if(keyPressed.contains(KeyEvent.VK_CONTROL) && keyPressed.contains(KeyEvent.VK_3))
				tabbedPane.setSelectedIndex(2);
			if(keyPressed.contains(KeyEvent.VK_CONTROL) && keyPressed.contains(KeyEvent.VK_4))
				tabbedPane.setSelectedIndex(3);
		}
	}

	public void keyReleased(KeyEvent e) {
		 keyPressed.remove(e.getKeyCode());
	}

	private static JPanel getTabbedMenu() {
		tabbedPane = new JTabbedPane();

		JPanel orthoTab = orthoTab();
		JPanel obliTab = obliTab();
		JPanel anoxTab = axonTab();
		JPanel persTab = persTab();

		tabbedPane.add("Ortogonal", orthoTab);
		tabbedPane.add(obliTab, "Obliqua");
		tabbedPane.add(anoxTab, "Axonometrica");
		tabbedPane.add(persTab, "Perspectiva");

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				if (tabbedPane.getSelectedIndex() == 0) {
					fourthQuadrant = getSelectedRadioButton();	//Metodo para Ortogonal
				}
				if (tabbedPane.getSelectedIndex() == 1) {
					fourthQuadrant = OBLIQUE;
					// Metodo para Obliqua
				}
				if (tabbedPane.getSelectedIndex() == 2) {
					fourthQuadrant = AXONOMETRIC;
					// Método para Axonométrica
				}
				if (tabbedPane.getSelectedIndex() == 3) {
					fourthQuadrant = PERSPECTIVE;
					// Método para Perspectiva
				}
				setViewProjection();
				canvas.requestFocus();
				canvas.repaint();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(tabbedPane);
		return panel;

	}

	private static JPanel orthoTab() {
		JPanel panel = new JPanel();
		group = new ButtonGroup();
		apRadioButton = new JRadioButton("Alcado Principal");
		pRadioButton = new JRadioButton("Planta");
		aeRadioButton = new JRadioButton("Alcado Esquerdo");
		adRadioButton = new JRadioButton("Alcado Direito");
		group.add(apRadioButton);
		group.add(pRadioButton);
		group.add(aeRadioButton);
		group.add(adRadioButton);

		class RadioButtonActionListener implements ItemListener {
	
			@Override
			public void itemStateChanged(ItemEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				if(button == apRadioButton &&  e.getStateChange() == ItemEvent.SELECTED)
					fourthQuadrant = FRONT_VIEW;
				else if(button == pRadioButton &&  e.getStateChange() == ItemEvent.SELECTED)
					fourthQuadrant = TOP_VIEW;
				else if(button == aeRadioButton &&  e.getStateChange() == ItemEvent.SELECTED) 
					fourthQuadrant = LEFT_VIEW;
				else if(button == adRadioButton &&  e.getStateChange() == ItemEvent.SELECTED) 
					fourthQuadrant = 7;
				setViewProjection();
				canvas.requestFocus();
				canvas.repaint();
				
			}
		}
		RadioButtonActionListener actionListener = new RadioButtonActionListener();
		apRadioButton.addItemListener(actionListener);
		pRadioButton.addItemListener(actionListener);
		aeRadioButton.addItemListener(actionListener);
		adRadioButton.addItemListener(actionListener);
		
		panel.add(apRadioButton);
		panel.add(pRadioButton);
		panel.add(aeRadioButton);
		panel.add(adRadioButton);
		return panel;
	}

	private static JPanel obliTab() {
		JPanel panel = new JPanel();

		JLabel alphaLabel = new JLabel("alpha = "+DEFAULT_ALPHA);
		JLabel lLabel = new JLabel("l = "+DEFAULT_L);

		sliderAlphaObliqua = new JSlider(JSlider.HORIZONTAL, 0, 360, DEFAULT_ALPHA);
		sliderAlphaObliqua.setMinorTickSpacing(45);
		sliderAlphaObliqua.setMajorTickSpacing(90);
		sliderAlphaObliqua.setPaintTicks(true);
		sliderAlphaObliqua.setPaintLabels(true);
		sliderAlphaObliqua.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				oProjection.setAlpha(sliderAlphaObliqua.getValue());
				alphaLabel.setText("alpha = "+sliderAlphaObliqua.getValue());
				setViewProjection();
				canvas.repaint();
			}
		});

		sliderLObliqua = new JSlider(JSlider.HORIZONTAL, 0, 10, (int)DEFAULT_L*10);
		sliderLObliqua.setMinorTickSpacing(1);
		sliderLObliqua.setMajorTickSpacing(5);
		sliderLObliqua.setPaintTicks(true);
		sliderLObliqua.setPaintLabels(true);
		sliderLObliqua.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				oProjection.setL((double)sliderLObliqua.getValue()/10);
				lLabel.setText(String.format("l = %.2f", (oProjection.getL()/10)));
				setViewProjection();
				canvas.repaint();
			}
		});
		
		panel.add(alphaLabel);
		panel.add(sliderAlphaObliqua);
		panel.add(lLabel);
		panel.add(sliderLObliqua);
		return panel;
	}

	private static JPanel axonTab() {
		JPanel panel = new JPanel();
		setThetaGamma();
		JLabel thetaLabel = new JLabel("theta = "+theta);
		JLabel gammaLabel = new JLabel("gamma = "+gamma);

		sliderGammaAxonometrica = new JSlider(JSlider.HORIZONTAL, 0, 360, gamma);
		sliderGammaAxonometrica.setMinorTickSpacing(45);
		sliderGammaAxonometrica.setMajorTickSpacing(90);
		sliderGammaAxonometrica.setPaintTicks(true);
		sliderGammaAxonometrica.setPaintLabels(true);
		sliderGammaAxonometrica.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gammaLabel.setText("gamma = "+sliderGammaAxonometrica.getValue());
				setViewProjection();
				canvas.repaint();
			}
		});

		sliderThetaAxonometrica = new JSlider(JSlider.HORIZONTAL, 0, 360, theta);
		sliderThetaAxonometrica.setMinorTickSpacing(45);
		sliderThetaAxonometrica.setMajorTickSpacing(90);
		sliderThetaAxonometrica.setPaintTicks(true);
		sliderThetaAxonometrica.setPaintLabels(true);
		sliderThetaAxonometrica.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				thetaLabel.setText("theta = "+sliderThetaAxonometrica.getValue());
				setViewProjection();
				canvas.repaint();
			}
		});
		
		panel.add(thetaLabel);
		panel.add(sliderThetaAxonometrica);
		panel.add(gammaLabel);
		panel.add(sliderGammaAxonometrica);
		return panel;
	}

	private static JPanel persTab() {
		JPanel panel = new JPanel();

		JLabel dLabel = new JLabel("d = "+DEFAULT_D);

		sliderDPerspectiva = new JSlider(JSlider.HORIZONTAL, 1, 50, DEFAULT_D);
		sliderDPerspectiva.setMinorTickSpacing(12);
		sliderDPerspectiva.setMajorTickSpacing(24);
		sliderDPerspectiva.setPaintTicks(true);
		sliderDPerspectiva.setPaintLabels(true);
		sliderDPerspectiva.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pProjection.setD(sliderDPerspectiva.getValue());
				dLabel.setText("d = "+(int)pProjection.getD());
				setViewProjection();
				canvas.repaint();
			}
		});
		
		panel.add(dLabel);
		panel.add(sliderDPerspectiva);
		return panel;
	}
	
	private static int getSelectedRadioButton() {
		if(apRadioButton.isSelected())
			return FRONT_VIEW;
		if(pRadioButton.isSelected())
			return TOP_VIEW;
		if(aeRadioButton.isSelected())
			return LEFT_VIEW;
		if(adRadioButton.isSelected())
			return RIGHT_VIEW;
		return OBLIQUE;
	}
	
	private static void setThetaGamma() {
		double t = Math.atan(Math.sqrt(Math.tan(Math.toRadians(DEFAULT_A)) / (Math.tan(Math.toRadians(DEFAULT_B))))) - (Math.PI/2);
		double g = Math.asin(Math.sqrt(Math.tan(Math.toRadians(DEFAULT_A))) * Math.tan(Math.toRadians(DEFAULT_B)));
		theta = (int) ((Math.toDegrees(t) + 360) % 360)+1;
		gamma = (int) ((Math.toDegrees(g) + 360) % 360)+1;
	}
	
	private static void setViewProjection() {
		if((alcadoPrincipal || alcadoEsquerdo || planta) && (!escape)) {
			alcadoPrincipal = false;
			alcadoEsquerdo = false;
			planta = false;
			projeccaoAdicional = true;
		}
	}
}
