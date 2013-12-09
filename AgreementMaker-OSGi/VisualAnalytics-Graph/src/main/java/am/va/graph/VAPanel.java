package am.va.graph;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import am.va.graph.VAVariables.ontologyType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

//import ensemble.Ensemble2;

public class VAPanel {

	private static JFrame frame;
	private static JFXPanel fxPanel;
	private static ListView<String> listView;
	private static Group root;
	private static VAGroup rootGroupLeft;
	private static VAGroup rootGroupRight;
	private static VAGroup previousGroup;
	private static VAGroup currentGroup;
	private static int stop = -1;

	private static Button btnRoot;
	private static Button btnUp;
	private static Button btnHelp;

	private static Label lblSource;
	private static Label lblTarget;

	private static VAPieChart chartLeft;
	private static VAPieChart chartRight;
	private static Tooltip pieTooltip;
	private static VASearchBox searchBox;

	/**
	 * Init Frame
	 */
	public static void initAndShowGUI() {
		frame = new JFrame("VA");
		frame.setSize(1100, 550);
		frame.setLocation(100, 100);
		fxPanel = new JFXPanel();
		frame.add(fxPanel);
		frame.setVisible(true);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				InitFX();
			}
		});
	}

	/**
	 * Init JavaFx panel, add mouse click Eventhandler
	 */
	public static void InitFX() {
		root = new Group();

		Scene myScene = new Scene(root);

		// Main layout: BorderPane
		BorderPane borderPane = new BorderPane();

		// left side: listView
		listView = new ListView<String>();
		listView.setPrefHeight(500);
		listView.setPrefWidth(100);
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		borderPane.setLeft(listView);

		// Top side: HBox, contains toolbar
		ToolBar toolbar = new ToolBar();
		Region spacer = new Region();
		spacer.setStyle("-fx-padding: 0 8em 0 0;");
		spacer.getStyleClass().setAll("spacer");
		HBox buttonBar = new HBox();

		// set three buttons
		btnRoot = new Button("Top level");
		btnUp = new Button("Go up");
		btnHelp = new Button("Help");
		setButtonActions();
		buttonBar.getChildren().addAll(btnRoot, btnUp, btnHelp);

		// set search box
		BorderPane searchboxborderPane = new BorderPane();
		searchBox = new VASearchBox();
		searchBox.getStyleClass().add("search-box");
		searchboxborderPane.setRight(searchBox);

		Region spacer2 = new Region();
		spacer2.setStyle("-fx-padding: 0 40em 0 0;");
		spacer2.getStyleClass().setAll("spacer");
		HBox.setMargin(searchBox, new Insets(0, 5, 0, 0));
		toolbar.getItems().addAll(spacer, buttonBar, spacer2,
				searchboxborderPane);
		borderPane.setTop(toolbar);

		// Center side: two piecharts as a group, tilepane layout is used
		Group chartGroup = new Group();
		TilePane tilePane = new TilePane();
		tilePane.setPrefColumns(2); // preferred columns

		// set two pies
		chartLeft = new VAPieChart(rootGroupLeft);
		chartLeft.getPieChart().setClockwise(false);
		chartRight = new VAPieChart(rootGroupRight);
		lblSource = new Label("Source ontology", chartLeft.getPieChart());
		lblSource.setContentDisplay(ContentDisplay.TOP);
		lblTarget = new Label("Target ontology", chartRight.getPieChart());
		lblTarget.setContentDisplay(ContentDisplay.TOP);

		// tooltip
		pieTooltip = new Tooltip("click to view more");
		for (final PieChart.Data currentData : chartLeft.getPieChart()
				.getData()) {
			Tooltip.install(currentData.getNode(), getPieTooltip());
		}
		tilePane.getChildren().add(lblSource);
		tilePane.getChildren().add(lblTarget);
		chartGroup.getChildren().add(tilePane);
		borderPane.setCenter(chartGroup);

		root.getChildren().add(borderPane);
		fxPanel.setScene(myScene);

		// update pie data
		updatePreviousGroup(rootGroupLeft);
		updateCurrentGroup(rootGroupLeft);
		setLocation(chartLeft);
		// TEST(currentGroup);
		chartLeft.updatePieChart(ontologyType.Source);

		// myScene.getStylesheets().add(Ensemble2.class.getResource("ensemble2.css").toExternalForm());
		// myScene.getStylesheets().add(VAPanel.class.getResource("VA.css").toExternalForm());
	}

	/**
	 * Set radius and center point for Pie chart
	 * 
	 * @param chart
	 */
	private static void setLocation(VAPieChart chart) {
		// TODO Auto-generated method stub
		double minX = Double.MAX_VALUE;
		double maxX = Double.MAX_VALUE * -1;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MAX_VALUE * -1;

		for (PieChart.Data d : chart.getPieChart().getData()) {
			minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
			maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
			minY = Math.min(minY, d.getNode().getBoundsInParent().getMinY());
			maxY = Math.max(maxY, d.getNode().getBoundsInParent().getMaxY());
		}

		double radius = (maxX - minX) / 2;
		chart.setRadius(radius);
		chart.setPieCenter(new Point2D(minX + radius, minY + radius));
		// System.out.println("radius " + radius + " center "+
		// chart.getPieCenter());
	}

	/**
	 * Generate new VAGroup according user's click
	 * 
	 * @param currentGroup
	 * @return
	 */
	public static void getNewGroup(VAVariables.ontologyType ontologyType) {
		// Need a function here, return value:VAData
		VAData newRootData = VAPieChart.getSelectedVAData();
		System.out.println("New data " + newRootData.getNodeName());
		VAGroup newGroup = new VAGroup();
		newGroup.setRootNode(newRootData);
		if (newRootData != null && newRootData.hasChildren()) {
			newGroup.setParent(currentGroup.getGroupID());
			newGroup.setListVAData(VASyncData.getChildrenData(newRootData,
					ontologyType));
		} else {
			newGroup.setParent(previousGroup.getGroupID());
		}
		updateCurrentGroup(newGroup);
	}

	private static void updatePreviousGroup(VAGroup group) {
		if (group != null) {
			if (btnUp.isDisable()) {
				btnUp.setDisable(false);
			}
			previousGroup = group;

		} else {
			System.out.println("- Previous group is empty ?!!");
		}
	}

	/**
	 * Update current group
	 * 
	 * @param group
	 */
	private static void updateCurrentGroup(VAGroup group) {
		if (stop != -1)
			updatePreviousGroup(currentGroup);
		currentGroup = group;
	}

	/**
	 * Init rootGroup
	 * 
	 * @param group
	 */
	public static void setRootGroupLeft(VAGroup group) {
		rootGroupLeft = group;
	}

	public static void setRootGroupRight(VAGroup group) {
		rootGroupRight = group;
	}

	/**
	 * Print info. Testing only.
	 * 
	 * @param rootGroup
	 */
	public static void testVAGroup(VAGroup rootGroup) {
		System.out.println("-----------------------------");
		if (rootGroup != null) {
			String rootNodeName = rootGroup.getRootNode().getNodeName();
			System.out.println(rootNodeName);
			ArrayList<VAData> vaData = rootGroup.getVADataArray();
			for (VAData d : vaData) {
				System.out.println(d.getNodeName() + ","
						+ d.getTargetNode().getLocalName() + ","
						+ d.getSimilarity());
			}
			HashMap<String, Integer> slots = rootGroup.getslotCountMap();
			for (String s : VAVariables.thresholdName) {
				if (slots.containsKey(s))
					System.out.println(s + ":" + slots.get(s));
			}
		} else {
			System.out.println("Empty group ?!!");
		}
	}

	public static VAGroup getCurrentGroup() {
		return currentGroup;
	}

	public static int getStop() {
		return stop;
	}

	public static Group getFXGroup() {
		return root;
	}

	public static Tooltip getPieTooltip() {
		return pieTooltip;
	}

	public static void setPieTooltip(Tooltip pieTooltip) {
		VAPanel.pieTooltip = pieTooltip;
	}

	public static void setStop(int i) {
		stop = i;
	}

	public static ListView<String> getlistView() {
		return listView;
	}

	public static void setListView(ListView<String> list) {
		listView = list;
	}

	public static void setSourceLabel(String label) {
		lblSource.setText(label);
	}

	public static void setTargetLabel(String label) {
		lblTarget.setText(label);
	}
	
	public static VAPieChart getRightPie(){
		return chartRight;
	}
	
	public static VAGroup getRightGroup(){
		return rootGroupRight;
	}

	/**
	 * Add event for buttons
	 */
	private static void setButtonActions() {
		/**
		 * Go to root panel
		 */
		btnRoot.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				updateCurrentGroup(rootGroupLeft);
				chartLeft.updatePieChart(ontologyType.Source);
				System.out.println("Go to root panel");
			}

		});

		/**
		 * Go to previous panel (can only click once)
		 */
		btnUp.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				updateCurrentGroup(previousGroup);
				chartLeft.updatePieChart(ontologyType.Source);
				System.out.println("Go to previous panel");
				btnUp.setDisable(true);
			}

		});
	}
}
