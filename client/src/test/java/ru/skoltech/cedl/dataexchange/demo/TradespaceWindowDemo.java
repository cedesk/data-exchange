package ru.skoltech.cedl.dataexchange.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.skoltech.cedl.dataexchange.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.controller.TradespaceController;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.SimpleSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.tradespace.TradespaceFactory;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceWindowDemo extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupContext();

        URL url = TradespaceWindowDemo.class.getResource("/GPUdataset_2015.csv");
        File file = new File(url.getFile());
        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.buildFromCSV(file);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.TRADESPACE_WINDOW);
        Parent root = loader.load();
        TradespaceController controller = loader.getController();
        controller.setModel(multitemporalTradespace);

        primaryStage.setTitle("Tradespace Window Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();

        closeContext();
    }

    private void closeContext() {
        try {
            ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
            Project project = context.getBean(Project.class);
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
            project.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void setupContext() {
        try {
            ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
            Project project = context.getBean(Project.class);
            UnitManagement unitManagement = context.getBean(UnitManagementService.class).loadDefaultUnitManagement();
            Study study = new Study("TS Demo");
            Field sField = Project.class.getDeclaredField("study");
            sField.setAccessible(true);
            sField.set(project, study);

            SimpleSystemBuilder simpleSystemBuilder = new SimpleSystemBuilder();
            simpleSystemBuilder.setUnitManagement(unitManagement);
            SystemModel systemModel = simpleSystemBuilder.build(study.getName());
            study.setSystemModel(systemModel);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
