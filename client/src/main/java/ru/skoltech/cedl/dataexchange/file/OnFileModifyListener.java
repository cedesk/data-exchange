package ru.skoltech.cedl.dataexchange.file;

import java.io.File;
import java.util.function.Consumer;

/**
 * Created by D.Knoll on 07.07.2015.
 */
public class OnFileModifyListener implements SimpleDirectoryWatchService.OnFileChangeListener {

    private Consumer<File> fileConsumer;

    public OnFileModifyListener(Consumer<File> fileConsumer) {
        this.fileConsumer = fileConsumer;
    }

    @Override
    public void onFileCreate(String filePath) {
        // empty dummy
    }

    @Override
    public void onFileModify(String filePath) {
        fileConsumer.accept(new File(filePath));
    }

    @Override
    public void onFileDelete(String filePath) {
        // empty dummy
    }

}