package neu.nctracer.dm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.Match;
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

    private double alpha;

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
        String nearestNeighborsClass = params.getParam(PARAM_NEAREST_NEIGHBOR_CLASS);
        instantiateNeareastNeighborClass(nearestNeighborsClass);

        this.alpha = Double.parseDouble(params.getParam("correpondence.score.function.alpha",
                                                        "0.5"));

        // make a copy so it can be passed on to invoked classes
        this.params = params;
    }

    private void instantiateNeareastNeighborClass(String nearestNeighborsClass) {

        if (null == nearestNeighborsClass) {
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
     * 3. Calculates score based on matching translated source points and given
     * target points
     */
    public <T> Match findMatch(List<DataObject> source,
                               List<DataObject> target,
                               DataTransformation<T> transform) {

        Map<DataObject, DataObject> translatedObjects = translateSourceObjects(source, transform);

        // user might have set configurations for nearest neighbor. Thus pass
        // the config object to nearest neighbors class
        this.nearestNeighbors.setup(target, this.params);

        Queue<DataCorrespondence> minHeap = new PriorityQueue<>();

        for (Entry<DataObject, DataObject> entry : translatedObjects.entrySet()) {
            DataObject sourceObj = entry.getKey();
            DataObject translatedSourceObj = entry.getValue();

            Map<DataObject, Double> neighbors = this.nearestNeighbors.findNeighbors(translatedSourceObj,
                                                                                    50);
            if (null == neighbors || neighbors.isEmpty()) {
                continue;
            }

            // add all pairs (source-target points) to the priority queue to
            // greedily pick globally optimum matches (based on error)
            for (Entry<DataObject, Double> neighbor : neighbors.entrySet()) {
                DataObject targetObj = neighbor.getKey();
                Double distance = neighbor.getValue();
                minHeap.offer(new DataCorrespondence(sourceObj, targetObj, distance));
            }
        }

        Match match = generateMatch(minHeap, source, target);
        return match;
    }

    private Match generateMatch(Queue<DataCorrespondence> minHeap,
                                List<DataObject> source,
                                List<DataObject> target) {
        Set<DataObject> unusedSourceObjects = new HashSet<>(source);
        Set<DataObject> unusedTargetObjects = new HashSet<>(target);

        Set<DataCorrespondence> correspondences = new LinkedHashSet<>();
        double score = Double.MIN_VALUE;

        // number of correspondences found so far
        int matchCount = 0;

        // loop until we find all distinct pairs
        while (!minHeap.isEmpty()
               && !unusedSourceObjects.isEmpty()
               && !unusedTargetObjects.isEmpty()) {

            DataCorrespondence correspondence = minHeap.poll();

            DataObject sourceObj = correspondence.getSource();
            DataObject targetObj = correspondence.getTarget();

            // use this pair only if source and target points are not
            // already used by other processed pairs
            if (unusedSourceObjects.contains(sourceObj)
                && unusedTargetObjects.contains(targetObj)) {

                // remove so that following pairs don't use them again
                unusedSourceObjects.remove(sourceObj);
                unusedTargetObjects.remove(targetObj);

                // generate score for this correspondence
                double currentScore = generateScore(++matchCount, correspondence.getError());

                // keep the score if the match is good or we haven't at least
                // looked at couple of matches
                if (currentScore >= score) {
                    score = currentScore;
                    correspondences.add(correspondence);
                } else {
                    // Generated score wasn't good. We have found all good
                    // matches.
                    break;
                }
            }
        }

        return new Match(score, correspondences);
    }

    /**
     * Scoring function is: <tt>(alpha * size) - ((1 - alpha) * error)</tt>
     * 
     * @param position
     *            - correspondence position in the generated match list sorted
     *            by minimum error
     * @param error
     *            - error incurred in generating the matching correspondence
     * 
     * @return - double value generated using specified formula
     */
    private double generateScore(int position, double error) {
        double penaltyFactor = (1 - this.alpha);
        return (this.alpha * position) - (penaltyFactor * error);
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
