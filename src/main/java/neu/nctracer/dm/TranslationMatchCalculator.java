package neu.nctracer.dm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.Match;
import neu.nctracer.dm.conf.ConfigurationParams;
import neu.nctracer.dm.conf.DMConfigurationHandler;
import neu.nctracer.exception.ReflectionUtilsException;
import neu.nctracer.utils.ReflectionUtils;

public class TranslationMatchCalculator implements MatchCalculator {

    /*
     * Configurable parameters - can be overridden by the client
     */
    // determines the class used for determining correspondence
    public static final String PARAM_NEAREST_NEIGHBOR_CLASS = "nearest.neighbor.class";

    private NearestNeighbors nearestNeighbors = null;
    private ConfigurationParams params;

    public TranslationMatchCalculator() {
    }

    /**
     * Registers classes and other parameters to be used by the algorithm.
     * Caller can specify class names for following parameters in the
     * <code>ConfigurationParams</code> object <br>
     * 1. "nearest.neighbor.class" = defaults to {@link KNearestNeighbors} <br>
     * <br>
     * 
     * All other parameters are passed on to the invoked classes
     * 
     * @param params
     *            - Configuration parameters
     */
    public void setup(ConfigurationParams params) {
        String nearestNeighborsClass = params.getParam(PARAM_NEAREST_NEIGHBOR_CLASS,
                                                       KNearestNeighbors.class.getName());
        instantiateNeareastNeighborClass(nearestNeighborsClass);
        // make a copy so it can be passed on to invoked classes
        this.params = copyParams(params);
    }

    private static ConfigurationParams copyParams(ConfigurationParams params) {
        Map<String, String> configMap = params.getParams();
        ConfigurationParams paramsInstance = DMConfigurationHandler.getHandler()
                                                                   .getConfigurationParamsInstance();
        paramsInstance.setParams(configMap);
        return paramsInstance;
    }

    private void instantiateNeareastNeighborClass(String nearestNeighborsClass) {

        if (nearestNeighborsClass.equals(KNearestNeighbors.class.getName())) {
            // load default class
            this.nearestNeighbors = new KNearestNeighbors();
        } else {
            // load client specified class
            try {
                this.nearestNeighbors = ReflectionUtils.instantiate(nearestNeighborsClass,
                                                                    NearestNeighbors.class);
            } catch (ReflectionUtilsException e) {
                throw new RuntimeException("Error initializing class ["
                                           + nearestNeighborsClass
                                           + "]");
            }
        }
    }

    /**
     * 1. Applies transformation on given source objects <br>
     * 2. Finds correspondences between transformed source objects and target
     * objects <br>
     * 3. Calculates error in matching translated source points and given target
     * points
     */
    public <T> Match findMatch(List<DataObject> source,
                               List<DataObject> target,
                               DataTransformation<T> transform) {

        Map<DataObject, DataObject> translatedObjects = translateSourceObjects(source, transform);

        this.nearestNeighbors.setup(target, this.params);

        Set<DataCorrespondence> correspondences = new HashSet<>();

        double error = 0;
        for (Entry<DataObject, DataObject> entry : translatedObjects.entrySet()) {
            Map<DataObject, Double> neighbors = this.nearestNeighbors.findNeighbors(entry.getValue(),
                                                                                    1);
            if (null == neighbors || neighbors.isEmpty()) {
                continue;
            }
            Entry<DataObject, Double> neighbor = neighbors.entrySet().iterator().next();

            // TODO: accommodate error for missing points
            error += neighbor.getValue();
            DataCorrespondence correspondence = new DataCorrespondence(entry.getKey(),
                                                                       neighbor.getKey());
            correspondences.add(correspondence);
        }

        Match match = new Match();
        match.setCorrespondences(correspondences);
        match.setError(error / correspondences.size());

        return match;
    }

    private <T>
            Map<DataObject, DataObject>
            translateSourceObjects(List<DataObject> source, DataTransformation<T> transform) {

        Map<DataObject, DataObject> translatedObjects = new HashMap<>(source.size());
        double[] translation = computeTranslation(transform);
        for (DataObject sourceObj : source) {
            DataObject translatedObj = sourceObj.deepClone();
            double[] features = translatedObj.getFeatures();
            for (int i = 0; i < features.length; i++) {
                features[i] += translation[i];
            }

            translatedObjects.put(sourceObj, translatedObj);
        }
        return translatedObjects;
    }

    private <T> double[] computeTranslation(DataTransformation<T> transform) {
        double[] directionAngles = transform.getAngles();
        double distance = transform.getDistance();
        double[] translation = new double[directionAngles.length];

        for (int i = 0; i < translation.length; i++) {
            translation[i] = distance * Math.cos(Math.toRadians(directionAngles[i]));
        }

        return translation;
    }
}
