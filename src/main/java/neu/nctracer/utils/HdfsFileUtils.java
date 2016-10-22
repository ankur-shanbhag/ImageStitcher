package neu.nctracer.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;

import neu.nctracer.exception.HdfsException;

/**
 * Utility class to perform HDFS I/O operation such as read, write, copy, delete
 * etc.
 * 
 * @author Ankur Shanbhag
 *
 */
public final class HdfsFileUtils {

    public static String readFileAsString(Configuration conf, String filePath)
            throws HdfsException {
        byte[] bytes = readFile(conf, filePath);
        return new Text(bytes).toString();
    }

    public static byte[] readFile(Configuration conf, String filePath) throws HdfsException {
        FSDataInputStream in = null;
        try {
            FileSystem fs = FileSystem.get(URI.create(filePath), conf);

            // open the file for reading
            in = fs.open(new Path(filePath));

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            IOUtils.copyBytes(in, bytes, conf, false);
            return bytes.toByteArray();

        } catch (IOException e) {
            throw new HdfsException("Error while reading HDFS file.", e);
        } catch (Exception e) {
            throw new HdfsException("Unexpected error occured while reading hdfs file.", e);
        } finally {
            IOUtils.closeStream(in);
        }
    }

    public static boolean createDir(String path, boolean overwrite, Configuration conf)
            throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(path), conf);
            Path hdfsDir = new Path(path);

            if (overwrite)
                return fs.delete(hdfsDir, true) ? fs.mkdirs(hdfsDir) : false;
            else
                return fs.mkdirs(hdfsDir);

        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static boolean delete(String path, boolean recursive, Configuration conf)
            throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(path), conf);
            return fs.exists(new Path(path)) ? fs.delete(new Path(path), recursive) : true;

        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static void copyFromLocal(String localDirPath,
                                     String hdfsDirPath,
                                     Configuration conf)
            throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(hdfsDirPath), conf);

            if (fs.exists(new Path(hdfsDirPath)) && !fs.delete(new Path(hdfsDirPath), true))
                throw new HdfsException("Error deleting destination path.");

            fs.copyFromLocalFile(false, true, new Path(localDirPath), new Path(hdfsDirPath));
        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static void copyToLocal(String hdfsPath, String localPath, Configuration conf) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(hdfsPath), conf);

            Path srcPath = new Path(hdfsPath);
            Path dstPath = new Path(localPath);
            if (!fs.exists(srcPath))
                throw new HdfsException("Hdfs path [" 
                                        + hdfsPath
                                        + "] does not exist. Cannot perform copy to local.");

            fs.copyToLocalFile(srcPath, dstPath);
        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static Path[] listFilePaths(String path, Configuration conf) throws HdfsException {
        try {
            FileSystem fs = FileSystem.get(URI.create(path), conf);
            FileStatus[] fileStatus = fs.listStatus(new Path(path));

            Path[] filePaths = new Path[fileStatus.length];

            for (int i = 0; i < fileStatus.length; i++) {
                filePaths[i] = fileStatus[i].getPath();
            }
            return filePaths;
        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    private HdfsFileUtils() {
        // Deny object creation
    }
}