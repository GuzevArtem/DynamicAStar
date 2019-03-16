package edu.kpi.iasa.ai;

import edu.kpi.iasa.ai.configuration.Attribute;
import edu.kpi.iasa.ai.configuration.Configuration;
import edu.kpi.iasa.ai.configuration.ConfiguredObstacle;
import edu.kpi.iasa.ai.gui.*;
import edu.kpi.iasa.ai.gui.obstacles.PointObstacle;
import edu.kpi.iasa.ai.model.AStarWorker;
import edu.kpi.iasa.ai.model.ObstacleContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;


public class Main {
    private static Configuration configuration = new Configuration();

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private static DrawZone zone;

    private static AWindow window;

    private static Thread executor;

    private static ObstacleContainer obstacleContainer;

    private static AStarWorker worker;

    private static JTextArea results;

    public static void main(String[] args) {
        createExecutor();
        SwingUtilities.invokeLater(() -> {
            init();  // Let the constructor do the job
        });
    }

    private static boolean simulating = true;

    private static void createExecutor() {
        System.out.println("[EXECUTOR created]");
        executor = new Thread(()->{
            if(zone != null) zone.clear();
            synchronized (executor) {
                try {
                    executor.wait();
                } catch (InterruptedException ignored) {}
            }
            worker.start();
            zone.setGraph(Converter.graph(worker.getGraph(),zone));
            simulating = true;
            window.getFrame().repaint();
            try {
                //pause for user
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            try {
                int iter = 0;
                int max_iters = configuration.getValue(Attribute.MAX_ITERATIONS_COUNT);
                while (simulating && worker.iter() && (max_iters == 0 || iter <= max_iters)) {
                    //draw result
                    zone.setPathTree(Converter.tree(worker.getPathTree(),zone));
                    results.setText("ITERATION "+iter);
                    window.getFrame().repaint();
                    try {
                        Thread.sleep(Long.valueOf(""+configuration.getValue(Attribute.SLEEP_TIME_PER_STEP)));
                    } catch (InterruptedException ignored) {
                        break;//if interrupted we stop cycle
                    }
                    iter++;
                }
                zone.setBestPath(Converter.path(worker.getBestPath(),zone));
                results.setText(results.getText()+"\n"+Arrays.toString(worker.getBestPath().toArray()));
                window.getFrame().repaint();
            }
            catch (Exception e){
                //catching all exception to perform normal flow
                e.printStackTrace();
            }
        });
        executor.setDaemon(true);
        executor.start();
    }

    private static void init() {
        window = new AWindow();
        window.init("AntSliderTest", WINDOW_WIDTH, WINDOW_HEIGHT, new FlowLayout());

        //creating panels
        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
        JPanel buttonsPanel = new JPanel(new FlowLayout());

        //creating sliders
        // lambda - function called on each value change
        ConfiguredSlider<Integer> maxIterationCount = new ConfiguredIntegerSlider(
                value -> configuration.setValue(Attribute.MAX_ITERATIONS_COUNT, value)
                , 6)
                .init(Attribute.MAX_ITERATIONS_COUNT, 0, Integer.MAX_VALUE, 100)
                .finish();
        ConfiguredSlider<Integer> shapeSize = new ConfiguredIntegerSlider(
                value -> {
                    configuration.setValue(Attribute.SHAP_SIZE, value);
                    if(zone != null)
                        zone.setShapeSize(value);
                }
                , 6)
                .init(Attribute.SHAP_SIZE, 0, 100, 10)
                .finish();
        ConfiguredSlider<Integer> iterSleepTime = new ConfiguredIntegerSlider(
                value -> configuration.setValue(Attribute.SLEEP_TIME_PER_STEP, value)
                , 6)
                .init(Attribute.SLEEP_TIME_PER_STEP, 0, 1000, 10)
                .finish();

        obstacleContainer = new ObstacleContainer();
        //zone for redrawing
        zone = new DrawZone(25, obstacleContainer);

        //initial configuration
        final ConfiguredObstacle startPoint = new ConfiguredObstacle(
                new PointObstacle(zone, new Point2D.Double(0.05,0.05))
        );
        final ConfiguredObstacle endPoint = new ConfiguredObstacle(
                new PointObstacle(zone, new Point2D.Double(0.95,0.95))
        );
        obstacleContainer.addNonBlocking(startPoint);
        obstacleContainer.addNonBlocking(endPoint);
        configuration.register(Attribute.OBSTACLE_CONTAINER, obstacleContainer);
        configuration.register(Attribute.MAX_ITERATIONS_COUNT, maxIterationCount);
        configuration.register(Attribute.SHAP_SIZE, shapeSize);
        configuration.register(Attribute.SLEEP_TIME_PER_STEP, iterSleepTime);

        //adding elements on panel
        paramsPanel.setAutoscrolls(true);
        paramsPanel.add(new JLabel("Setup simulation", JLabel.CENTER));
        paramsPanel.add(maxIterationCount.toJComponent());
        paramsPanel.add(shapeSize.toJComponent());
        paramsPanel.add(iterSleepTime.toJComponent());

        //creating buttons
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        Button generateButton = new Button("Build graph");
        Button clearButton = new Button("Clear");
        Button applyButton = new Button("Apply");
        Button cancelButton = new Button("Cancel");

        //setup buttons actions
        startButton.addActionListener(action -> {
            System.err.println("START");

            configuration.save();//apply changes

            obstacleContainer.setModifiable(false);
            //disable const sliders
            maxIterationCount.setModifiable(false);

            //and buttons
            generateButton.setEnabled(false);
            applyButton.setEnabled(false);
            cancelButton.setEnabled(false);
            clearButton.setEnabled(false);

            window.getFrame().repaint();

            startButton.setEnabled(false);
            stopButton.setEnabled(true); //enable stopper
            //starting new thread
            synchronized (executor) {
                executor.notifyAll();
            }
        });
        stopButton.addActionListener(action -> {
            simulating = false;
            System.err.println("STOP");
            //worker.stop();

            executor.stop();
            executor = null;
            createExecutor(); //creating new executor

            obstacleContainer.setModifiable(true);

            stopButton.setEnabled(false);//disable current button
            startButton.setEnabled(true);
            clearButton.setEnabled(true);

            //enable const sliders
            maxIterationCount.setModifiable(true);

            //and buttons
            generateButton.setEnabled(true);
            applyButton.setEnabled(true);
            cancelButton.setEnabled(true);
        });
        generateButton.addActionListener(action -> {
            worker.init();
            worker.rebuildVisibilityGraph();
            zone.setGraph(Converter.graph(worker.getGraph(), zone));
            window.getFrame().repaint();
        });
        clearButton.addActionListener(action -> {
            obstacleContainer.getNonBlocking().clear();
            obstacleContainer.getObstacles().clear();
            final ConfiguredObstacle startPointN = new ConfiguredObstacle(
                    new PointObstacle(zone, new Point2D.Double(0.05,0.05))
            );
            final ConfiguredObstacle endPointN = new ConfiguredObstacle(
                    new PointObstacle(zone, new Point2D.Double(0.95,0.95))
            );
            worker.setup(startPointN, endPointN);
            zone.setPoints(startPointN, endPointN);
            obstacleContainer.addNonBlocking(startPointN);
            obstacleContainer.addNonBlocking(endPointN);
            worker.init();
            zone.setGraph(Converter.graph(worker.getGraph(), zone));
            zone.repaint();
        });
        applyButton.addActionListener(action -> {
            configuration.save();
            window.getFrame().repaint();
        });
        cancelButton.addActionListener(action->{
            configuration.cancel();
            window.getFrame().repaint();
        });

        //disabled by default
        stopButton.setEnabled(false);

        //adding buttons on panel
        buttonsPanel.add(startButton);
        buttonsPanel.add(stopButton);
        buttonsPanel.add(generateButton);
        buttonsPanel.add(clearButton);
        buttonsPanel.add(applyButton);
        buttonsPanel.add(cancelButton);

        //add buttons
        paramsPanel.add(buttonsPanel);

        results = new JTextArea();
        results.setLineWrap(true);
        results.setColumns(10);
        results.setEditable(false);
        results.setAutoscrolls(true);
        results.setText("ITERATION 0");
        paramsPanel.add(results);

        //setup worker
        worker = new AStarWorker(obstacleContainer);
        worker.init();

        //content pane
        JPanel contentPane = new JPanel(new BorderLayout());

        //add resize listener
        contentPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resizeAntZoneOnWindowResize(e.getComponent().getWidth(), e.getComponent().getHeight(),25);
            }
        });

        //adding all on window
        contentPane.setLayout(new BorderLayout());
        contentPane.add(zone, BorderLayout.CENTER);
        contentPane.add(paramsPanel, BorderLayout.EAST);
        window.getFrame().setContentPane(contentPane);
        window.getFrame().setMinimumSize(new Dimension(800,600));
        window.finish();

        worker.setup(startPoint,endPoint);
        zone.setPoints(startPoint,endPoint);
        zone.repaint(zone.getAZoneSize());
    }

    private static void resizeAntZoneOnWindowResize(final int newWidth, final int newHeight) {
        resizeAntZoneOnWindowResize(newWidth, newHeight, 0);
    }

    private static void resizeAntZoneOnWindowResize(final int newWidth, final int newHeight, final int padding) {
        zone.setAZoneSize(new Rectangle(padding,padding,newWidth-330-2*padding,newHeight-2*padding));
        window.getFrame().repaint();
    }
}
