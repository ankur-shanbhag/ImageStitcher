package neu.nctracer.mr;

public final class HdfsConstants {

    public static final String SOURCE_IMAGE_HDFS_PATH = "hadoop.image.source.file";
    public static final String TARGET_IMAGE_HDFS_PATH = "hadoop.image.target.file";
    public static final String IMAGE_MATCHING_ERROR = "image.matching.error.threshold";

    private HdfsConstants() {
        // deny object creation
    }
}
