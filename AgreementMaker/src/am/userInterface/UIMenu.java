package am.userInterface;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import am.AMException;
import am.GlobalStaticVariables;
import am.Utility;
import am.app.Core;
import am.app.feedback.ui.SelectionPanel;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.AlignmentSet;
import am.app.mappingEngine.MatchersRegistry;
import am.app.mappingEngine.manualMatcher.UserManualMatcher;
import am.app.ontology.Ontology;
import am.userInterface.table.MatchersTablePanel;
import am.userInterface.vertex.VertexDescriptionPane;


public class UIMenu implements ActionListener {
	
	// create Top Level menus
	private JMenu fileMenu, viewMenu, helpMenu, matchingMenu, ontologyMenu;
	
	// File menu.
	private JMenuItem xit, openSource, openTarget, openMostRecentPair,
					  closeSource, closeTarget;
	private JMenu menuRecentSource, menuRecentTarget;
	
	// Edit menu.
	//private JMenuItem undo, redo;
	
	// View menu.
	private JMenuItem colorsItem, itemViewsCanvas;
	private JCheckBoxMenuItem smoMenuItem;  // Menu item for toggling "Selected Matchings Only" view mode.
	private JMenu menuViews;  // Views submenu.  TODO: Rename this to something more descriptive.
	
	// Ontology menu.
	private JMenuItem ontologyDetails;
	
	// Matching menu.
	private JMenuItem manualMapping, userFeedBack, 
					  newMatching, runMatching, copyMatching, deleteMatching, clearAll, 
					  doRemoveDuplicates, 
					  saveMatching, 
					  refEvaluateMatching;
	
	
	// Help menu.
	private JMenuItem howToUse, aboutItem;		
	
	

	//creates a menu bar
	private JMenuBar myMenuBar;
	
	private UI ui;  // reference to the main ui.
	

	
	
	private JCheckBoxMenuItem disableVisualizationItem;
	private JCheckBoxMenuItem showLabelItem;
	private JCheckBoxMenuItem showLocalNameItem;

	
	public UIMenu(UI ui){
		this.ui=ui;
		init();
		
	}
	
	
	public void refreshRecentMenus() {
		refreshRecentMenus( menuRecentSource, menuRecentTarget);
	}
	
	/**
	 * This function will update the Recent File Menus with the most up to date recent files
	 * @param recentsource
	 * @param recenttarget
	 */
	private void refreshRecentMenus( JMenu recentsource, JMenu recenttarget ) {
		
		AppPreferences prefs = ui.getAppPreferences();
		
		// first we start by removing all sub menus
		recentsource.removeAll();
		recenttarget.removeAll();
		
		// then populate the menus again.
		for( int i = 0; i < prefs.countRecentSources(); i++) {
			JMenuItem menuitem = new JMenuItem(i + ".  " + prefs.getRecentSourceFileName(i));
			menuitem.setActionCommand("source" + i);
			menuitem.setMnemonic( 48 + i);
			menuitem.addActionListener(this);
			menuRecentSource.add(menuitem);
		}
		
		for( int i = 0; i < prefs.countRecentTargets(); i++) {
			JMenuItem menuitem = new JMenuItem(i + ".  " + prefs.getRecentTargetFileName(i));
			menuitem.setActionCommand("target" + i);
			menuitem.setMnemonic( 48 + i);
			menuitem.addActionListener(this);
			menuRecentTarget.add(menuitem);
		}
		
	}
	
	
	
	public void actionPerformed (ActionEvent ae){
		try {
			Object obj = ae.getSource();
			MatchersControlPanel controlPanel = ui.getControlPanel();
			if (obj == xit){
				// confirm exit
				confirmExit();
				// if it is no, then do nothing		
			}else if (obj == colorsItem){
				new Legend();	
			}else if (obj == howToUse){
				Utility.displayTextAreaPane(Help.getHelpMenuString(), "Help");
			}else if (obj == openSource){
				openAndReadFilesForMapping(GlobalStaticVariables.SOURCENODE);
				if( Core.getInstance().sourceIsLoaded() ) {
					openSource.setEnabled(false);
					menuRecentSource.setEnabled(false);
					closeSource.setEnabled(true);
				}
			}else if (obj == openTarget){
				openAndReadFilesForMapping(GlobalStaticVariables.TARGETNODE);
				if( Core.getInstance().targetIsLoaded() ) {
					openTarget.setEnabled(false);
					menuRecentTarget.setEnabled(false);
					closeTarget.setEnabled(true);
				}
			}else if (obj == openMostRecentPair){
				AppPreferences prefs = new AppPreferences();
				int position = 0;
				ui.openFile( prefs.getRecentSourceFileName(position), GlobalStaticVariables.SOURCENODE, 
						prefs.getRecentSourceSyntax(position), prefs.getRecentSourceLanguage(position), prefs.getRecentSourceSkipNamespace(position), prefs.getRecentSourceNoReasoner(position));
				ui.openFile( prefs.getRecentTargetFileName(position), GlobalStaticVariables.TARGETNODE, 
						prefs.getRecentTargetSyntax(position), prefs.getRecentTargetLanguage(position), prefs.getRecentTargetSkipNamespace(position), prefs.getRecentTargetNoReasoner(position));
				closeSource.setEnabled(true);
				closeTarget.setEnabled(true);
				
				openSource.setEnabled(false);
				openTarget.setEnabled(false);
				menuRecentSource.setEnabled(false);
				menuRecentTarget.setEnabled(false);
				openMostRecentPair.setEnabled(false);
				
			}else if (obj == aboutItem){
				new AboutDialog();
				//displayOptionPane("Agreement Maker 3.0\nAdvis research group\nThe University of Illinois at Chicago 2004","About Agreement Maker");
			}
			else if( obj == disableVisualizationItem ) {
				// Save the SMO setting that has been changed
				AppPreferences prefs = ui.getAppPreferences();
				boolean disableVis = disableVisualizationItem.isSelected();
				prefs.saveSelectedMatchingsOnly(disableVis);
				ui.getCanvas().setDisableVisualization(disableVis);
				ui.redisplayCanvas();
			}
			else if( obj == smoMenuItem ) {
				// Save the SMO setting that has been changed
				AppPreferences prefs = ui.getAppPreferences();
				boolean smoStatus = smoMenuItem.isSelected();
				prefs.saveSelectedMatchingsOnly(smoStatus);
				ui.getCanvas().setSMO(smoStatus);
				ui.redisplayCanvas();
			}
			else if( obj == showLabelItem || obj == showLocalNameItem ) {
				// Save the setting that has been changed
				AppPreferences prefs = ui.getAppPreferences();
				boolean showLabel = showLabelItem.isSelected();
				prefs.saveShowLabel(showLabel);
				ui.getCanvas().setShowLabel(showLabel);
				boolean showLocalname = showLocalNameItem.isSelected();
				prefs.saveShowLocalname(showLocalname);
				ui.getCanvas().setShowLocalName(showLocalname);
				ui.redisplayCanvas();
			}
			else if( obj == userFeedBack ) {
				SelectionPanel sp = new SelectionPanel(ui);
				sp.showScreen_Start();
				ui.addTab("User Feedback Loop", null, sp, "User Feedback Loop");	
			}
			else if( obj == manualMapping) {
				Utility.displayMessagePane("To edit or create a manual mapping select any number of source and target nodes.\nLeft click on a node to select it, use Ctrl and/or Shift for multiple selections.", "Manual Mapping");
			}
			else if(obj == newMatching) {
				controlPanel.newManual();
			}
			else if(obj == runMatching) {
				controlPanel.matchSelected();
			}
			else if(obj == copyMatching) {
				controlPanel.copy();
			}
			else if(obj == deleteMatching) {
				controlPanel.delete();
			}
			else if(obj == saveMatching) {
				controlPanel.export();
			}
			else if(obj == refEvaluateMatching) {
				controlPanel.evaluate();
			}
			else if(obj == clearAll) {
				controlPanel.clearAll();
			}
			else if(obj == ontologyDetails) {
				ontologyDetails();
			}
			else if( obj == itemViewsCanvas ) {

				JPanel CanvasPanel = new JPanel();
				CanvasPanel.setLayout(new BorderLayout());
				
				Canvas goodOldCanvas = new Canvas(ui);
				goodOldCanvas.setFocusable(true);
				
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setWheelScrollingEnabled(true);
				scrollPane.getVerticalScrollBar().setUnitIncrement(20);
				
				scrollPane.setViewportView(goodOldCanvas);
				goodOldCanvas.setScrollPane(scrollPane);
				
				JPanel jPanel = null;
				System.out.println("opening file");
				jPanel = new VertexDescriptionPane(GlobalStaticVariables.ONTFILE);//takes care of fields for XML files as well
			    jPanel.setMinimumSize(new Dimension(200,480));
		
				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, jPanel);
				splitPane.setOneTouchExpandable(true);
				splitPane.setResizeWeight(1.0);
				splitPane.setMinimumSize(new Dimension(640,480));
				splitPane.setPreferredSize(new Dimension(640,480));
				splitPane.getLeftComponent().setPreferredSize(new Dimension(640,480));
				// add scrollpane to the panel and add the panel to the frame's content pane
				
				CanvasPanel.add(splitPane, BorderLayout.CENTER);
				//frame.getContentPane().add(splitPane, BorderLayout.CENTER);

				
				//panelControlPanel = new ControlPanel(this, uiMenu, canvas);
				MatchersControlPanel matcherControlPanel = new MatchersControlPanel(ui, this);
				CanvasPanel.add(matcherControlPanel, BorderLayout.PAGE_END);
				//frame.getContentPane().add(matcherControlPanel, BorderLayout.PAGE_END);
				
				
				ui.addTab("Canvas View", null, CanvasPanel, "Canvas View");
				
			}
			else if( obj == doRemoveDuplicates ) {
				// TODO: Get rid of this from here.
				
				MatchersTablePanel m = controlPanel.getTablePanel();
				
				int[] selectedRows =  m.getTable().getSelectedRows();
				
				if(selectedRows.length != 2) {
					Utility.displayErrorPane("You must select two matchers.", null);
				}
				else {

					int i, j;
					
					Core core = Core.getInstance();
					
					AbstractMatcher firstMatcher = core.getMatcherInstances().get(selectedRows[0]);
					AbstractMatcher secondMatcher = core.getMatcherInstances().get(selectedRows[1]);
					
					AlignmentSet<Alignment> firstClassSet = firstMatcher.getClassAlignmentSet();
					AlignmentSet<Alignment> secondClassSet = secondMatcher.getClassAlignmentSet();
					
					AlignmentSet<Alignment> firstPropertiesSet = firstMatcher.getPropertyAlignmentSet();
					AlignmentSet<Alignment> secondPropertiesSet = secondMatcher.getPropertyAlignmentSet();
					
					AlignmentSet<Alignment> combinedClassSet = new AlignmentSet<Alignment>();
					AlignmentSet<Alignment> combinedPropertiesSet = new AlignmentSet<Alignment>();

					// double nested loop, later I will write a better algorithm -cos
					for( i = 0; i < firstClassSet.size(); i++ ) {
						Alignment candidate = firstClassSet.getAlignment(i);
						boolean foundDuplicate = false;
						for( j = 0; j < secondClassSet.size(); j++ ) {
							Alignment test = secondClassSet.getAlignment(j);							
						
						
							int sourceNode1 = candidate.getEntity1().getIndex();
							int targetNode1 = candidate.getEntity2().getIndex();
						
							int sourceNode2 = test.getEntity1().getIndex();
							int targetNode2 = test.getEntity2().getIndex();
						
							if(sourceNode1 == sourceNode2 && targetNode1 == targetNode2 ) {
								// we found a duplicate
								foundDuplicate = true;
								break;	
							}
						}
						
						if( !foundDuplicate ) combinedClassSet.addAlignment(candidate);
						
					}

					for( i = 0; i < secondClassSet.size(); i++ ) {
						Alignment candidate = secondClassSet.getAlignment(i);
						boolean foundDuplicate = false;
						for( j = 0; j < firstClassSet.size(); j++ ) {
							Alignment test = firstClassSet.getAlignment(j);							
						
						
							int sourceNode1 = candidate.getEntity1().getIndex();
							int targetNode1 = candidate.getEntity2().getIndex();
						
							int sourceNode2 = test.getEntity1().getIndex();
							int targetNode2 = test.getEntity2().getIndex();
						
							if(sourceNode1 == sourceNode2 && targetNode1 == targetNode2 ) {
								// we found a duplicate
								foundDuplicate = true;
								break;	
							}
						}
						
						if( !foundDuplicate ) combinedClassSet.addAlignment(candidate);
						
					}
					
					
					// now the properties.
					
					// double nested loop, later I will write a better algorithm -cos
					for( i = 0; i < firstPropertiesSet.size(); i++ ) {
						Alignment candidate = firstPropertiesSet.getAlignment(i);
						boolean foundDuplicate = false;
						for( j = 0; j < secondPropertiesSet.size(); j++ ) {
							Alignment test = secondPropertiesSet.getAlignment(j);							
						
						
							int sourceNode1 = candidate.getEntity1().getIndex();
							int targetNode1 = candidate.getEntity2().getIndex();
						
							int sourceNode2 = test.getEntity1().getIndex();
							int targetNode2 = test.getEntity2().getIndex();
						
							if(sourceNode1 == sourceNode2 && targetNode1 == targetNode2 ) {
								// we found a duplicate
								foundDuplicate = true;
								break;	
							}
						}
						
						if( !foundDuplicate ) combinedPropertiesSet.addAlignment(candidate);
						
					}

					for( i = 0; i < secondPropertiesSet.size(); i++ ) {
						Alignment candidate = secondPropertiesSet.getAlignment(i);
						boolean foundDuplicate = false;
						for( j = 0; j < firstPropertiesSet.size(); j++ ) {
							Alignment test = firstPropertiesSet.getAlignment(j);							
						
						
							int sourceNode1 = candidate.getEntity1().getIndex();
							int targetNode1 = candidate.getEntity2().getIndex();
						
							int sourceNode2 = test.getEntity1().getIndex();
							int targetNode2 = test.getEntity2().getIndex();
						
							if(sourceNode1 == sourceNode2 && targetNode1 == targetNode2 ) {
								// we found a duplicate
								foundDuplicate = true;
								break;	
							}
						}
						
						if( !foundDuplicate ) combinedPropertiesSet.addAlignment(candidate);
						
					}
					
					
					AbstractMatcher newMatcher = new UserManualMatcher();
					
					newMatcher.setClassesAlignmentSet(combinedClassSet);
					newMatcher.setPropertiesAlignmentSet(combinedClassSet);
					newMatcher.setName(MatchersRegistry.UniqueMatchings);
					
					m.addMatcher(newMatcher);
					
					
					
				}
				
				
				
			} else if( obj == closeSource ) {
				if( Core.getInstance().targetIsLoaded() ) {
					// confirm with the user that we should reset matchings
					if( controlPanel.clearAll() ) {
						Core.getInstance().removeOntology( Core.getInstance().getSourceOntology() );
						closeSource.setEnabled(false); // the source ontology has been removed, grey out the menu entry
						// and we need to enable the source ontology loading menu entries
						openSource.setEnabled(true);
						menuRecentSource.setEnabled(true);
					}
				} else {
					// if there is no target loaded, we don't have to reset matchings.
					//controlPanel.resetMatchings();
					Core.getInstance().removeOntology( Core.getInstance().getSourceOntology() );
					closeSource.setEnabled(false);  // the source ontology has been removed, grey out the menu entry
					// and we need to enable the source ontology loading menu entries
					openSource.setEnabled(true);
					menuRecentSource.setEnabled(true);
					ui.redisplayCanvas();
				}
				if( !Core.getInstance().sourceIsLoaded() && !Core.getInstance().targetIsLoaded() ) openMostRecentPair.setEnabled(true);
			} else if( obj == closeTarget ) {
				if( Core.getInstance().sourceIsLoaded() ) {
					// confirm with the user that we should reset any matchings
					if( controlPanel.clearAll() ) {
						Core.getInstance().removeOntology( Core.getInstance().getTargetOntology() );
						closeTarget.setEnabled(false); // the target ontology has been removed, grey out the menu entry
						// and we need to enable the target ontology loading menu entries
						openTarget.setEnabled(true);
						menuRecentTarget.setEnabled(true);

					}
				} else {
					// if there is no source ontology loaded, we don't have to ask the user
					Core.getInstance().removeOntology( Core.getInstance().getTargetOntology() );
					closeTarget.setEnabled(false);  // the target ontology has been removed, grey out the menu entrys
					// and we need to enable the target ontology loading menu entries
					openTarget.setEnabled(true);
					menuRecentTarget.setEnabled(true);
					ui.redisplayCanvas();
				}
				if( !Core.getInstance().sourceIsLoaded() && !Core.getInstance().targetIsLoaded() ) openMostRecentPair.setEnabled(true);
			}
			
			
			
			// TODO: find a Better way to do this
			
			String command = ae.getActionCommand();  // get the command string we set
			if( command.length() == 7 ) { // the only menus that set an action command  are the recent menus, so we're ok.
				
				AppPreferences prefs = new AppPreferences();
				
				char index[] = new char[1];  // '0' - '9'
				char ontotype[] = new char[1]; // 's' or 't' (source or target)
				
				command.getChars(0, 1 , ontotype, 0);  // get the first character of the sting
				command.getChars(command.length() - 1, command.length(), index, 0); // get the last character of the string
				
				// based on the first and last characters of the action command, we can tell which menu was clicked.
				// the rest is easy
				
				int position = index[0] - 48; // 0 - 9
				switch( ontotype[0] ) {
					
					case 's':
						ui.openFile( prefs.getRecentSourceFileName(position), GlobalStaticVariables.SOURCENODE, 
								prefs.getRecentSourceSyntax(position), prefs.getRecentSourceLanguage(position), prefs.getRecentSourceSkipNamespace(position), prefs.getRecentSourceNoReasoner(position));
						prefs.saveRecentFile(prefs.getRecentSourceFileName(position), GlobalStaticVariables.SOURCENODE, 
								prefs.getRecentSourceSyntax(position), prefs.getRecentSourceLanguage(position), prefs.getRecentSourceSkipNamespace(position), prefs.getRecentSourceNoReasoner(position));
						ui.getUIMenu().refreshRecentMenus(); // after we update the recent files, refresh the contents of the recent menus.
						
						// Now that we have loaded a source ontology, disable all the source ontology loading menu entries ...
						openSource.setEnabled(false);
						menuRecentSource.setEnabled(false);
						openMostRecentPair.setEnabled(false);
						// ... and enable the close menu entry 
						closeSource.setEnabled(true);
						
						break;
					case 't':
						ui.openFile( prefs.getRecentTargetFileName(position), GlobalStaticVariables.TARGETNODE, 
								prefs.getRecentTargetSyntax(position), prefs.getRecentTargetLanguage(position), prefs.getRecentTargetSkipNamespace(position), prefs.getRecentTargetNoReasoner(position));
						prefs.saveRecentFile(prefs.getRecentTargetFileName(position), GlobalStaticVariables.TARGETNODE, 
								prefs.getRecentTargetSyntax(position), prefs.getRecentTargetLanguage(position), prefs.getRecentTargetSkipNamespace(position), prefs.getRecentTargetNoReasoner(position));
						ui.getUIMenu().refreshRecentMenus(); // after we update the recent files, refresh the contents of the recent menus.
						
						// Now that we have loaded a source ontology, disable all the source ontology loading menu entries ...
						openTarget.setEnabled(false);
						menuRecentTarget.setEnabled(false);
						openMostRecentPair.setEnabled(false);
						// ... and enable the close menu entry 
						closeTarget.setEnabled(true);

						
						break;
					default:
						break;
				}
				
			}
		}
		catch(AMException ex2) {
			Utility.displayMessagePane(ex2.getMessage(), null);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Utility.displayErrorPane(Utility.UNEXPECTED_ERROR, null);
		}
		
	}
	
	public void ontologyDetails() {
		Core c = Core.getInstance();
		Ontology sourceO = c.getSourceOntology();
		Ontology targetO = c.getTargetOntology();
		String sourceClassString = "Not loaded\n";
		String sourcePropString = "Not loaded\n";
		String targetClassString = "Not loaded\n";
		String targetPropString = "Not loaded\n";
		if(c.sourceIsLoaded()) {
			sourceClassString = sourceO.getClassDetails();
			sourcePropString = sourceO.getPropDetails();
		}
		if(c.targetIsLoaded()) {
			targetClassString = targetO.getClassDetails();
			targetPropString = targetO.getPropDetails();
		}
		String report = "Ontology details\n\n";
		report+= "Hierarchies             \t#concepts\tdepth\tUC-diameter\tLC-diameter\t#roots\t#leaves\n";
		report+= "Source Classes:\t"+sourceClassString;
		report+= "Target Classes:\t"+targetClassString;
		report+= "Source Properties:\t"+sourcePropString;
		report+= "Target Properties:\t"+targetPropString;
		Utility.displayTextAreaWithDim(report,"Reference Evaluation Report", 10, 60);
	}


	public void displayOptionPane(String desc, String title){
			JOptionPane.showMessageDialog(null,desc,title, JOptionPane.PLAIN_MESSAGE);					
	}
	
	
	public void init(){
		
		// need AppPreferences for smoItem, to get if is checked or not.
		AppPreferences prefs = new AppPreferences();
		
		//Creating the menu bar
		myMenuBar = new JMenuBar();
		ui.getUIFrame().setJMenuBar(myMenuBar);

		// building the file menu
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		myMenuBar.add(fileMenu);	

		//add openGFile menu item to file menu
		openSource = new JMenuItem("Open Source Ontology...",new ImageIcon("../images/fileImage.gif"));
		//openSource.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));                		
		//openSource.setMnemonic(KeyEvent.VK_O);
		openSource.addActionListener(this);
		fileMenu.add(openSource);
		
		//add openGFile menu item to file menu
		openTarget = new JMenuItem("Open Target Ontology...",new ImageIcon("../images/fileImage.gif"));
		//openTarget.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));                		
		//openTarget.setMnemonic(KeyEvent.VK_O);
		openTarget.addActionListener(this);
		fileMenu.add(openTarget);

		// add separator
		fileMenu.addSeparator();
		
		// Construct the recent files menu.
		menuRecentSource = new JMenu("Recent Sources...");
		menuRecentSource.setMnemonic('u');
		
		menuRecentTarget = new JMenu("Recent Targets...");
		menuRecentTarget.setMnemonic('a');
		
		
		refreshRecentMenus(menuRecentSource, menuRecentTarget);
		
/*		
		menuRecentSourceList = new JMenuItem[10];
		Preferences prefs = Preferences.userRoot().node("/com/advis/agreementMaker");
		int lastsynt = prefs.getInt(PREF_LASTSYNT, 0);
		int lastlang = prefs.getInt(PREF_LASTLANG, 1);
		*/
		//menuRecentSource.add( new JMenu());
		
		fileMenu.add(menuRecentSource);
		fileMenu.add(menuRecentTarget);
		openMostRecentPair = new JMenuItem("Open most recent pair");
		openMostRecentPair.addActionListener(this);
		fileMenu.add(openMostRecentPair);
		fileMenu.addSeparator();
		//private JMenuItem menuRecentSource, menuRecentTarget;
		//private JMenuItem menuRecentSourceList[], menuRecentTargetList[]; // the list of recent files
		closeSource = new JMenuItem("Close Source Ontology");
		closeSource.addActionListener(this);
		closeSource.setEnabled(false); // there is no source ontology loaded at the beginning
		closeTarget = new JMenuItem("Close Target Ontology");
		closeTarget.addActionListener(this);
		closeTarget.setEnabled(false); // there is no target ontology loaded at the beginning
		fileMenu.add(closeSource);
		fileMenu.add(closeTarget);
		
		
		fileMenu.addSeparator();
		// add exit menu item to file menu
		xit = new JMenuItem("Exit", KeyEvent.VK_X);
		xit.addActionListener(this);
		fileMenu.add(xit);
		
		
		// Build view menu in the menu bar: TODO
		viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		myMenuBar.add(viewMenu);

		//All show and hide details has been removed right now
		// add separator
		//viewMenu.addSeparator();

		// add keyItem 
		colorsItem = new JMenuItem("Colors",KeyEvent.VK_K);
		colorsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK)); 	                
		colorsItem.addActionListener(this);
		viewMenu.add(colorsItem);
		
		viewMenu.addSeparator();
		
		// add "Disable Visualization" option to the view menu
		disableVisualizationItem = new JCheckBoxMenuItem("Disable hierarchies visualization");
		disableVisualizationItem.addActionListener(this);
		disableVisualizationItem.setSelected(prefs.getDisableVisualization());
		viewMenu.add(disableVisualizationItem);
		
		// add "Selected Matchings Only" option to the view menu
		smoMenuItem = new JCheckBoxMenuItem("Selected Matchings Only");
		smoMenuItem.addActionListener(this);
		smoMenuItem.setSelected(prefs.getSelectedMatchingsOnly());
		viewMenu.add(smoMenuItem);
		
		showLocalNameItem = new JCheckBoxMenuItem("Show localnames");
		showLocalNameItem.addActionListener(this);
		showLocalNameItem.setSelected(prefs.getShowLocalname());
		viewMenu.add(showLocalNameItem);
		
		showLabelItem = new JCheckBoxMenuItem("Show labels");
		showLabelItem.addActionListener(this);
		showLabelItem.setSelected(prefs.getShowLabel());
		viewMenu.add(showLabelItem);
		
		viewMenu.addSeparator();
		
		menuViews = new JMenu("New view");
		itemViewsCanvas = new JMenuItem("Canvas");
		itemViewsCanvas.addActionListener(this);
		//itemViewsCanvas2 = new JMenuItem("Canvas2");
		//itemViewsCanvas2.addActionListener(this);
		menuViews.add(itemViewsCanvas);
		//menuViews.add(itemViewsCanvas2);
		viewMenu.add(menuViews);
		//Fake menus..********************************.
		/*

		evaluationMenu = new JMenu("Evaluation");
		myMenuBar.add(ontologyMenu);


		*/
		
		//ontology menu
		ontologyMenu = new JMenu("Ontology");
		ontologyMenu.setMnemonic('O');
		ontologyDetails = new JMenuItem("Ontology details");
		ontologyDetails.addActionListener(this); 
		ontologyMenu.add(ontologyDetails);
		myMenuBar.add(ontologyMenu);
		
		matchingMenu = new JMenu("Matching");
		matchingMenu.setMnemonic('M');
		manualMapping = new JMenuItem("Manual Mapping"); 
		manualMapping.addActionListener(this);
		matchingMenu.add(manualMapping);
		
		userFeedBack = new JMenuItem("User Feedback Loop");
		userFeedBack.addActionListener(this);
		//matchingMenu.add(userFeedBack); Removed to distribute the AM
		
		matchingMenu.addSeparator();
		newMatching = new JMenuItem("New empty matching");
		newMatching.addActionListener(this);
		matchingMenu.add(newMatching);
		runMatching = new JMenuItem("Run selected matcher");
		runMatching.addActionListener(this);
		matchingMenu.add(runMatching);
		copyMatching = new JMenuItem("Copy selected matchings");
		copyMatching.addActionListener(this);
		matchingMenu.add(copyMatching);
		deleteMatching = new JMenuItem("Delete selected matchings");
		deleteMatching.addActionListener(this);
		matchingMenu.add(deleteMatching);
		clearAll = new JMenuItem("Clear All");
		clearAll.addActionListener(this);
		matchingMenu.add(clearAll);
		matchingMenu.addSeparator();
		
		doRemoveDuplicates = new JMenuItem("Remove Duplicate Alignments");
		doRemoveDuplicates.addActionListener(this);
		matchingMenu.add(doRemoveDuplicates);
		matchingMenu.addSeparator();
		
		saveMatching = new JMenuItem("Save selected matchings into a file");
		saveMatching.addActionListener(this);
		matchingMenu.add(saveMatching);
		matchingMenu.addSeparator();
		refEvaluateMatching = new JMenuItem("Evaluate with reference file");
		refEvaluateMatching.addActionListener(this);
		matchingMenu.add(refEvaluateMatching);
		myMenuBar.add(matchingMenu);
		
		
		// Build help menu in the menu bar.
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		myMenuBar.add(helpMenu);

		// add menu item to help menu
		howToUse = new JMenuItem("Help", new ImageIcon("images/helpImage.gif"));
		howToUse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));                	
		howToUse.setMnemonic(KeyEvent.VK_H);
		howToUse.addActionListener(this);
		helpMenu.add(howToUse);

		// add about item to help menu
		aboutItem = new JMenuItem("About Agreement Maker", new ImageIcon("images/aboutImage.gif"));
		aboutItem.setMnemonic(KeyEvent.VK_A);
		aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));                
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);
		

	}
	

	/**
	 * This method reads the XML or OWL files and creates trees for mapping
	 */	
	 public void openAndReadFilesForMapping(int fileType){
		new OpenOntologyFileDialog(fileType, ui);
	 }

	
	 
	 /**
	  * Function that is called when to user wants to close the program. 
	  */
	 public void confirmExit() {
		int n = JOptionPane.showConfirmDialog(null,"Are you sure you want to exit ?","Exit Agreement Maker",JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION)
		{
			System.out.println("Exiting the program.\n");
			System.exit(0);   
		}
	 }
}
