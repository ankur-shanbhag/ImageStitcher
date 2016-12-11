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
 * Utility class to perform file operation such as read, write, copy, delete
 * etc. on HDFS and local file system
 * 
 * @author Ankur Shanbhag
 *
 */
public final class HdfsFileUtils {

    /**
     * Reads text file data from the file specified as parameter
     * 
     * @param conf
     * @param filePath
     * @param isHdfsPath
     * @return
     * @throws HdfsException
     */
    public static String readFileAsString(Configuration conf,
                                          String filePath,
                                          boolean isHdfsPath) throws HdfsException {
        byte[] bytes = readFile(conf, filePath, isHdfsPath);
        return new Text(bytes).toString();
    }

    public static byte[] readFile(Configuration conf,
                                  String filePath,
                                  boolean isHdfsPath) throws HdfsException {
        FSDataInputStream in = null;
        try {
            FileSystem fs = getFileSystem(conf, filePath, isHdfsPath);

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

    public static FileSystem getFileSystem(Configuration conf,
                                           String filePath,
                                           boolean isHdfsPath) throws HdfsException {
        try {
            if (isHdfsPath)
                return FileSystem.get(URI.create(filePath), conf);

            // return local file system
            return FileSystem.getLocal(conf);
        } catch (IOException exp) {
            throw new HdfsException(exp);
        }
    }

    /**
     * Constructs path object from the given file/directory path with the URI
     * scheme prepended depending on the type of file-system the path
     * belongs<br>
     * Example1: <br>
     * Input - local path <tt>/home/hadoop</tt> <br>
     * Return - <tt>file:///home/hadoop</tt> <br>
     * <br>
     * Example2: <br>
     * Input - HDFS path <tt>/user/hadoop</tt> <br>
     * Return - <tt>hdfs:///user/hadoop</tt> <br>
     * 
     * @throws HdfsException
     */
    public static Path getPath(Configuration conf,
                               String path,
                               boolean isHdfsPath) throws HdfsException {
        FileSystem fileSystem = getFileSystem(conf, path, isHdfsPath);
        URI uri = fileSystem.getUri();
        return new Path(uri + path);
    }

    public static boolean createDir(String path,
                                    boolean overwrite,
                                    Configuration conf,
                                    boolean isHdfsPath) throws HdfsException {
        try {
            FileSystem fs = getFileSystem(conf, path, isHdfsPath);
            Path hdfsDir = new Path(path);

            if (overwrite)
                return fs.delete(hdfsDir, true) ? fs.mkdirs(hdfsDir) : false;
            else
                return fs.mkdirs(hdfsDir);

        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static boolean delete(String path,
                                 boolean recursive,
                                 Configuration conf,
                                 boolean isHdfsPath) throws HdfsException {
        try {
            FileSystem fs = getFileSystem(conf, path, isHdfsPath);
            return fs.exists(new Path(path)) ? fs.delete(new Path(path), recursive) : true;

        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static String copyFromLocal(String localPath,
                                       String hdfsPath,
                                       Configuration conf) throws HdfsException {
        try {
            FileSystem fs = getFileSystem(conf, hdfsPath, true);

            String hdfsCopiedPath = hdfsPath;
            if (isDir(hdfsPath, conf, true)) {
                // if hdfsPath already exists, a new file/directory is created
                // inside it with same name as localPath
                hdfsCopiedPath = hdfsPath + Path.SEPARATOR + new Path(localPath).getName();
            }

            fs.copyFromLocalFile(false, true, new Path(localPath), new Path(hdfsPath));

            return hdfsCopiedPath;
        } catch (IOException e) {
            throw new HdfsException("Error while copying local files to HDFS.", e);
        }
    }

    public static void copyToLocal(String hdfsPath,
                                   String localPath,
                                   Configuration conf) throws HdfsException {
        try {
            FileSystem fs = getFileSystem(conf, hdfsPath, true);

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

    public static Path[]
           listFilePaths(String path, Configuration conf, boolean isHdfsPath) throws HdfsException {
        try {
            FileSystem fs = getFileSystem(conf, path, isHdfsPath);
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

    public static boolean isDir(String filePath,
                                Configuration conf,
                                boolean isHdfsPath) throws HdfsException {
        try {
            FileSystem fs = getFileSystem(conf, filePath, isHdfsPath);
            Path path = new Path(filePath);
            return fs.exists(path) && fs.isDirectory(path);
        } catch (IOException exp) {
            throw new HdfsException("Error checking if HDFS path ["
                                    + filePath
                                    + "] is a directory",
                                    exp);
        }
    }

    private HdfsFileUtils() {
        // Deny object creation
    }
}

