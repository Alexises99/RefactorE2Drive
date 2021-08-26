package com.example.refactore2drive.sessions;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Session {
    private final String name;
    private final String[] comments;
    private final String startHour;
    private String endHour;
    private final CSVWriter writer;
    private static final int WRITE_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_PERMISSION = 0;


    public Session(String name, String[] comments, String path, String[] headers, String[] info) throws IOException, ErrorSDException {
        this.comments = comments;
        this.startHour = formatter(LocalDateTime.now());
        this.name = name;
        Boolean[] bools = checkExternalStorage();
        if (!(bools[0] == false || bools[1] == false)) {
            //TODO el directorio no funciona
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "e2drive" + File.separator + this.name);
            if (!file.exists()) {
                this.writer = new CSVWriter(new FileWriter(file, false), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            } else {
                this.writer = new CSVWriter(new FileWriter(file, true), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            }
            this.writer.writeNext(comments);
            this.writer.writeNext(info);
            this.writer.writeNext(headers);
        } else {
            this.writer = null;
            throw new ErrorSDException("SDError");
        }
    }

    public String getName() {
        return name;
    }

    public String getEndHour() {
        return endHour;
    }

    public String getStartHour() {
        return startHour;
    }

    public String[] getComments() {
        return comments;
    }

    public String formatter(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(formatter);
    }

    public Boolean[] checkExternalStorage() {
        boolean isAvailable = false;
        boolean isWritable = false;
        boolean isReadable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Operation possible - Read and Write
            isAvailable = true;
            isWritable = true;
            isReadable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Operation possible - Read Only
            isAvailable = true;
            isWritable = false;
            isReadable = true;
        } else {
            // SD card not available
            isAvailable = false;
            isWritable = false;
            isReadable = false;
        }
        Boolean[] bools = {isAvailable, isWritable, isReadable};
        return bools;
    }

    public void writeData(String[] data) {
        this.writer.writeNext(data);
    }

    public boolean close() {
        try {
            this.writer.close();
            endHour = formatter(LocalDateTime.now());
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public class ErrorSDException extends Exception {
        public ErrorSDException(String err) {
            super(err);
        }

        public ErrorSDException(String err, Throwable thorw) {
            super(err, thorw);
        }
    }
}
