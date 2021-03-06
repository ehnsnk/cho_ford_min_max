package edu.emit.project.controller;

import edu.emit.project.ExceptionDialog;
import edu.emit.project.Log;
import edu.emit.project.Main;
import edu.emit.project.app.ResultTableRowFX;
import edu.emit.project.model.AbstractModel;
import edu.emit.project.model.FordFulkerson;
import edu.emit.project.model.Gamma;
import edu.emit.project.model.GraphColorifier;
import edu.emit.project.model.LambdaManager;
import edu.emit.project.model.Sommet;
import edu.emit.project.model.serializable.DataSerializable;
import edu.emit.project.model.serializable.GraphManager;
import edu.emit.project.view.shape.ArcFX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import edu.emit.project.view.shape.SommetFX;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 *
 * @author heniroger
 */
public class FordFulkersonFXController implements Initializable{
 // edu.emit.project.controller.FordFulkersonFXController 
    
    private FordFulkersonFXController controller = this;
    Main main  = new Main();

    
    @FXML private StackPane spX1;
    @FXML private StackPane spXi;
    @FXML private StackPane spXn;
    @FXML private CheckBox chkMaximize;
    
    //@FXML private Polygon triangle;
    //@FXML private Line line;
    //@FXML private Group fleche;
    
    
    @FXML private AnchorPane anchPaneGraph;
    @FXML private TableView<ResultTableRowFX> tabView;
    
    public ArrayList<ArcFX> arcFXList =new ArrayList<>();
    
     private TableColumn<ResultTableRowFX,String> tCIterationIndex; 
     private TableColumn<ResultTableRowFX,String> tCNodeIndex;
     private TableColumn<ResultTableRowFX,String> tCLambdaDiffValue;
     private TableColumn<ResultTableRowFX,String> tCArcValue;
     private TableColumn<ResultTableRowFX,String> tCLambdaJValue;
     
    private AbstractModel model;
    private ObservableList<ResultTableRowFX> data ;
    private  ArrayList<ResultTableRowFX> resultTableRowFXs = new ArrayList<>();
    
    private Boolean running = false;
    
    private  Group groupGraph = new Group();
    private static int i =1;
    
    private GraphManager graphManager = new GraphManager();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Shape shape = Shape.union(line, triangle);
        //fleche.getChildren().clear();
//        fleche.getChildren().add(shape);
               
        
        spXi.setDisable(false);
        setSpX1OnMousePressed();
        setSpXiOnMousePressed();
        setSpXnOnMousePressed();

        
        anchPaneGraph.getChildren().add(getGroupGraph());
        
        model = new FordFulkerson();
        data = FXCollections.observableArrayList();

        tCIterationIndex =new TableColumn<>("i");
        tCNodeIndex =new TableColumn<>("j");
        tCLambdaDiffValue =new TableColumn<>("\u03BBj - \u03BBi");
        tCArcValue =new TableColumn<>("v( xi , xj )");
        tCLambdaJValue =new TableColumn<>("\u03BBj");
        
        tCIterationIndex .setResizable(false);
        tCNodeIndex      .setResizable(false);
        tCLambdaDiffValue.setResizable(false);
        tCArcValue       .setResizable(false);
        tCLambdaJValue   .setResizable(false);
        
        tCIterationIndex .setPrefWidth(150);
        tCNodeIndex      .setPrefWidth(150);
        tCLambdaDiffValue.setPrefWidth(400);
        tCArcValue       .setPrefWidth(200);
        tCLambdaJValue   .setPrefWidth(400);
        
        tCIterationIndex  .setStyle("-fx-alignment:CENTER");
        tCNodeIndex       .setStyle("-fx-alignment:CENTER");
        tCLambdaDiffValue .setStyle("-fx-alignment:CENTER");
        tCArcValue        .setStyle("-fx-alignment:CENTER");
        tCLambdaJValue    .setStyle("-fx-alignment:CENTER");
        
        
        tCIterationIndex.setCellValueFactory(new PropertyValueFactory<ResultTableRowFX,String>("i"));
        tCNodeIndex.setCellValueFactory(new PropertyValueFactory<ResultTableRowFX,String>("j"));
        tCLambdaDiffValue.setCellValueFactory(new PropertyValueFactory<ResultTableRowFX,String>("diffLjLi"));
        tCArcValue.setCellValueFactory(new PropertyValueFactory<ResultTableRowFX,String>("vij"));
        tCLambdaJValue.setCellValueFactory(new PropertyValueFactory<ResultTableRowFX,String>("nlj"));
        
        tabView.getColumns().addAll(tCIterationIndex,tCNodeIndex,tCLambdaDiffValue,tCArcValue,tCLambdaJValue);

        
    }
    
    private boolean refresh =false;
    @FXML protected void btnStopAction(){
        if (getGraphManager()!= null) {
           setRunning(false);
           GraphColorifier graphColorifier = new GraphColorifier(null,getGraphManager());
           graphColorifier.decolorify();           
        }

    }
    @FXML protected void btnRunAction(){
        if (isRunning() || getGraphManager() == null) {
            return;
        }
        try {
            if (!chkMaximize.isSelected()) {
                data.clear();
                System.err.println("data:"+data.size());
                data = FXCollections.observableArrayList(getMinimization());
                tabView.setItems(data);


            }else{
                 data.clear();
                tabView.getItems().clear();
                data = FXCollections.observableArrayList(getMaximization());
                tabView.setItems(data);
            
            }
            
            setRunning(true);
            
            
        } catch (AbstractModel.OptimizationTypeException ex) {
            Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
        }
      
    }
    @FXML protected void btnOpenAction(){
        ObjectInputStream oIS =null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir un graphe");
        
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier Recherche Opérationnelle","*.ro"));
        
        File file = fileChooser.showOpenDialog(new Stage());
        if(file != null){
            try {
                final FileInputStream fileIS = new FileInputStream(file);
                oIS =new ObjectInputStream(fileIS);
                
                DataSerializable dataSerializable = (DataSerializable) oIS.readObject();
                setGraphManager(new GraphManager());
                getGraphManager().setDataSerializable(dataSerializable);
                getGraphManager().setController(controller);
                getGraphManager().convertToSommetFXList();
                getGraphManager().convertToArcFXList();
                getGraphManager().displayToGraph(getGroupGraph());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                if (oIS != null) {
                    try {
                        oIS.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        
        }
    }
    @FXML protected void btnSaveAction(){
        ObjectOutputStream oOS = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer");
        
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier Recherche Opérationnelle(*.ro)","*.ro"));
        fileChooser.setInitialFileName("*.ro");
        
        File file = fileChooser.showSaveDialog(new Stage());
        
        if(file != null){
            if(file.getName().endsWith(".ro")){
                try {
                    final FileOutputStream fileOS = new FileOutputStream(file);
                    oOS = new ObjectOutputStream(fileOS);
                    //oOS.writeUTF("Salut");
                    getGraphManager().convertToSommetSerializableList(getGraphManager().getSommetFXList());
                    getGraphManager().convertToArcSerializableList(getGraphManager().getArcFXList());
                    oOS.writeObject(getGraphManager().getDataSerializable());
                    oOS.flush();

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
                }finally{
                    if(oOS !=null){
                        try {
                            oOS.flush();
                            oOS.close();
                        } catch (IOException ex) {
                            Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            }else{
                try {
                    ExceptionDialog ed = new ExceptionDialog(
                            new Exception("Extension du fichier "+file.getName()+" est invalide."),
                            "Extension du fichier "+file.getName()+" est invalide."
                    );
                    
                } catch (Exception ex) {
                    Logger.getLogger(FordFulkersonFXController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    @FXML protected void btnClearAction(){
        enableSpX(spX1, TYPE_X1);
        disableSpX(spXi, TYPE_XI);
        disableSpX(spXn, TYPE_XN);
        main.sommetIndexEmpty.clear();

        setGraphManager(null);
        tabView.getItems().clear();
        getGroupGraph().getChildren().clear();
    }

    
    
    public void setNombreSommet(int nombre) throws AbstractModel.OptimizationTypeException{
        model.setNombreSommet(nombre);
        control();
    
    }
    public void setOptimisationType(int type){
        model.setOptimisationType(type);
    }
    public  AbstractModel getModel(){
        return this.model;
    }
    public  ArrayList<String[]> getModelResultRows(){
        return this.model.getResultRows();
    }
    public void setArc(int from,int to,double vij){
        try {
            model.setArc(from, to, vij);
            //System.out.println("X"+from+"->X"+to+" : v(xi,xj) "+vij);
        } catch (AbstractModel.NotZeroSommetIterationException ex) {
            ex.printStackTrace();
        }
    
    
    }
    public void control(){
        if (getModel().getOptimisationType()== FordFulkerson.MINIMIZATION_TYPE) {
            getModel().calculerMinimisation();
            ArrayList<Sommet> sommets =getModel().calculerCheminMinimal(getModel().getListSommet());
            GraphColorifier colorifier = new GraphColorifier(sommets, getGraphManager());
            LambdaManager lambdaManager = new LambdaManager(sommets, getGraphManager());
            lambdaManager.update();
            setController(this);
            colorifier.colorify();
        }else if(getModel().getOptimisationType()== FordFulkerson.MAXIMIZATION_TYPE){
            getModel().calculerMaximisation();
            ArrayList<Sommet> sommets =getModel().calculerCheminMinimal(getModel().getListSommet());
            GraphColorifier colorifier = new GraphColorifier(sommets, getGraphManager());
            LambdaManager lambdaManager = new LambdaManager(sommets, getGraphManager());
            lambdaManager.update();
            setController(this);
            colorifier.colorify();
        }else{
            Log.error(this);
        }
        
    }
    public final ArrayList<ResultTableRowFX> testMinimisation()throws AbstractModel.OptimizationTypeException{
            this.setOptimisationType(FordFulkerson.MINIMIZATION_TYPE);
            this.setNombreSommet(16);

            this.setArc(1, 2, 10);
            
            this.setArc(2, 3, 15);
            this.setArc(2, 4, 8);
            
            this.setArc(3, 6, 1);
            this.setArc(3, 11, 16);
            
            this.setArc(4, 3, 8);
            this.setArc(4, 5, 6);
            
            this.setArc(5, 9, 1);
            
            this.setArc(6, 5, 5);
            this.setArc(6, 7, 4);
            
            this.setArc(7, 8, 1);
            this.setArc(7, 11, 8);
            
            this.setArc(8, 7, 1);
            this.setArc(8, 10, 2);
            
            this.setArc(9, 8, 3);
            this.setArc(9, 10, 4);
            
            this.setArc(10, 12, 7);
            
            this.setArc(11, 12, 6);
            this.setArc(11, 13, 12);
            
            this.setArc(12, 15, 9);
            
            this.setArc(13, 14, 3);
            
            this.setArc(14, 16, 3);
            
            this.setArc(15, 14, 5);
            this.setArc(15, 16, 6);
            
            this.control();//Tsy maintsy asiana anio ein!! Tsy azo atao am place hafa
            
            for (int i = 0; i < this.getModelResultRows().size(); i++) {
                addResultTableRowFXs(new ResultTableRowFX(this.getModelResultRows().get(i)));
                
            }
            

       
        return getResultTableRowFXs();
    }
    public final ArrayList<ResultTableRowFX> getMinimization()throws AbstractModel.OptimizationTypeException{
        GraphManager  graphManager = getGraphManager();
        ArrayList<ArcFX> arcFXs = graphManager.getArcFXList();
        ArrayList<SommetFX> sommetFXs = graphManager.getSommetFXList();
        this.setOptimisationType(FordFulkerson.MINIMIZATION_TYPE);
        this.setNombreSommet(sommetFXs.size());

        ArrayList<Object[]> objects = new ArrayList<>();
        ArrayList<Object[]> obs = new ArrayList<>();
         Gamma gamma = new Gamma();

         for (int j = 0; j < arcFXs.size(); j++) {
            ArcFX arcFX = arcFXs.get(j);
            arcFX.getListSommets();
            
            objects.add(new Object[]{arcFX.getListSommets().get(0).getID(),arcFX.getListSommets().get(1).getID(),arcFX.getV_ij()});
   
        }
         obs = gamma.convertToArrangedArrayList(objects);
         
         for (int k = 0; k < obs.size(); k++) {
                 Integer from = (Integer)obs.get(k)[0];
                 Integer to  = (Integer)obs.get(k)[1];
                 Double vij  = (Double)obs.get(k)[2];
                this.setArc(from,to, vij);
                 
          }


        this.control();//Tsy maintsy asiana anio ein!! Tsy azo atao am place hafa

        System.err.println("getModelResultRows():"+this.getModelResultRows().size());
        if (getResultTableRowFXs().size()> 0) {
            System.err.println("getResultTableRowFXs() Bis:"+getResultTableRowFXs().size());
            getResultTableRowFXs().clear();
        }
        for (int i = 0; i < this.getModelResultRows().size(); i++) {
             //System.err.println("getResultTableRowFXs() Bis:"+getResultTableRowFXs().size());


            addResultTableRowFXs(new ResultTableRowFX(this.getModelResultRows().get(i)));

        }
        System.err.println("getResultTableRowFXs():"+this.getResultTableRowFXs().size());

            

       
        return getResultTableRowFXs();
    }
    public final ArrayList<ResultTableRowFX> getMaximization()throws AbstractModel.OptimizationTypeException{
         GraphManager  graphManager = getGraphManager();
        ArrayList<ArcFX> arcFXs = graphManager.getArcFXList();
        ArrayList<SommetFX> sommetFXs = graphManager.getSommetFXList();
        this.setOptimisationType(FordFulkerson.MAXIMIZATION_TYPE);
        this.setNombreSommet(sommetFXs.size());

        ArrayList<Object[]> objects = new ArrayList<>();
        ArrayList<Object[]> obs = new ArrayList<>();
         Gamma gamma = new Gamma();

         for (int j = 0; j < arcFXs.size(); j++) {
            ArcFX arcFX = arcFXs.get(j);
            arcFX.getListSommets();
            
            objects.add(new Object[]{arcFX.getListSommets().get(0).getID(),arcFX.getListSommets().get(1).getID(),arcFX.getV_ij()});
   
        }
         obs = gamma.convertToArrangedArrayList(objects);
         
         for (int k = 0; k < obs.size(); k++) {
                 Integer from = (Integer)obs.get(k)[0];
                 Integer to  = (Integer)obs.get(k)[1];
                 Double vij  = (Double)obs.get(k)[2];
                this.setArc(from,to, vij);
                 
          }


        this.control();//Tsy maintsy asiana anio ein!! Tsy azo atao am place hafa

        if (getResultTableRowFXs().size()> 0) {
                System.err.println("getResultTableRowFXs() Bis:"+getResultTableRowFXs().size());
                getResultTableRowFXs().clear();
        }
        for (int i = 0; i < this.getModelResultRows().size(); i++) {
           
            addResultTableRowFXs(new ResultTableRowFX(this.getModelResultRows().get(i)));

        }
            

       
        return getResultTableRowFXs();
    }    
    private void addResultTableRowFXs(ResultTableRowFX resultTableRowFX){
        resultTableRowFXs.add(resultTableRowFX);
    }
    private ArrayList<ResultTableRowFX> getResultTableRowFXs(){
        return this.resultTableRowFXs;
    }
    
    
    private void disableSpX(StackPane toolbarXi,int type){
        toolbarXi.setStyle("-fx-background-color:#BBBBBB");
        switch(type){
            case TYPE_X1:
                DISABLE_X1=true;
                break;
            case TYPE_XI:
                DISABLE_XI=true;
                break;
            case TYPE_XN:
                DISABLE_XN=true;
                break;
            default:
                break;
               
        }
           
    
    }
     private void enableSpX(StackPane toolbarXi,int type){
         toolbarXi.setCursor(Cursor.DEFAULT);
        toolbarXi.setStyle("-fx-background-color:#00000000");
        switch(type){
            case TYPE_X1:
                DISABLE_X1=false;
                break;
            case TYPE_XI:
                DISABLE_XI=false;
                break;
            case TYPE_XN:
                DISABLE_XN=false;
                break;
            default:
                break;
               
        }
           
    
    }

    private static boolean DISABLE_X1=false;
    private static boolean DISABLE_XI=true;
    private static boolean DISABLE_XN=true;
    private final static int TYPE_X1=0;
    private final static int TYPE_XI=1;
    private final static int TYPE_XN=2;
    
  
    /**
     * 
     * @return  ArrayList
     */
    public ArrayList<ArcFX> getArcFXs(){ return this.arcFXList;}
    
    public boolean lastArcFXIsFull(){
        if (arcFXList.size() <= 0) {
            return false;
        }
        ArcFX lastArcFx = arcFXList.get(this.arcFXList.size()-1);
      /*  if (lastArcFx.sommetFXs.size() ==2) {
            return true;
        }*/
       
        return false;
    
    }
     public ArcFX getLastArcFX(){
       return arcFXList.get(this.arcFXList.size()-1);
    
    }

    /**
     * @return the controller
     */
    public FordFulkersonFXController getController() {
        return controller;
    }

    /**
     * @param controller the controller to set
     */
    public void setController(FordFulkersonFXController controller) {
        this.controller = controller;
    }

    /**
     * @return the graphManager
     */
    public GraphManager getGraphManager() {
        return graphManager;
    }

    /**
     * @param graphManager the graphManager to set
     */
    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

    private void setSpX1OnMousePressed(){
         spX1.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (DISABLE_X1==false) {
                    main.sommetIndexEmpty.add(false);
                    SommetFX  x1 = new SommetFX();
                    x1.setApp(main);
                    x1.setTypeSommet(SommetFX.SOMMET_TYPE_X1);
                    x1.setFill(Color.DODGERBLUE);
                    x1.setID(main.sommetIndexEmpty.size());
                    x1.getLblName().setText("X"+String.valueOf(x1.getID()));
                    x1.setController(getController());
                    
                    getGroupGraph().getChildren().add(x1);
                    getGroupGraph().getChildren().add(x1.getLblName());
                    getGroupGraph().getChildren().add(x1.getLblLambda());
                    getGroupGraph().getChildren().add(x1.getTxfLambda());
                    
                    getGraphManager().getSommetFXList().add(x1);
                    disableSpX(spX1, TYPE_X1);
                    enableSpX(spXi, TYPE_XI);
                }
                

            }
        });
    }
    
    private void setSpXiOnMousePressed(){
        spXi.setOnMousePressed(new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event) {
                if (DISABLE_XI==false) {
                    main.sommetIndexEmpty.add(false);
                    SommetFX xi = new SommetFX();
                    xi.setApp(main);
                    xi.setID(main.sommetIndexEmpty.size());
                    xi.getLblName().setText("X"+String.valueOf(xi.getID()));
                    xi.setController(getController());
                    
                    getGroupGraph().getChildren().add(xi);
                    getGroupGraph().getChildren().add(xi.getLblName());
                    getGroupGraph().getChildren().add(xi.getLblLambda());
                    getGroupGraph().getChildren().add(xi.getTxfLambda());
                    
                    getGraphManager().getSommetFXList().add(xi);
                    ++i;
                    //System.err.println(i);
                    if (i>=3) {
                        enableSpX(spXn, TYPE_XN);
                    }
                }
                
            }
        });
    }
    private void setSpXnOnMousePressed(){
        spXn.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
              
              if (DISABLE_XN==false) {
                  main.sommetIndexEmpty.add(false);
                  SommetFX xn = new SommetFX();
                  xn.setApp(main);
                  xn.setTypeSommet(SommetFX.SOMMET_TYPE_XN);
                  xn.setFill(Color.DODGERBLUE);
                  xn.setID(main.sommetIndexEmpty.size());
                  xn.getLblName().setText("X"+String.valueOf(xn.getID()));
                  xn.setController(getController());
                    getGroupGraph().getChildren().add(xn);
                    getGroupGraph().getChildren().add(xn.getLblName());
                    getGroupGraph().getChildren().add(xn.getLblLambda());
                    getGroupGraph().getChildren().add(xn.getTxfLambda());
                
                    getGraphManager().getSommetFXList().add(xn);
                    disableSpX(spXi, TYPE_XI);
                    disableSpX(spXn, TYPE_XN);

              }
            }
        });
    }

    /**
     * @return the groupGraph
     */
    public Group getGroupGraph() {
        return groupGraph;
    }

    /**
     * @param groupGraph the groupGraph to set
     */
    public void setGroupGraph(Group groupGraph) {
        this.groupGraph = groupGraph;
    }

    /**
     * @return the running
     */
    public Boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(Boolean running) {
        this.running = running;
    }

}
