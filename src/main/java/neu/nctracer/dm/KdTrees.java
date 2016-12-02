package neu.nctracer.dm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.ImageData;
import neu.nctracer.utils.DataTransformer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;

/**
 * Algorithm to quickly find nearest neighbors in K-Dimensional space.
 * Guarantees logarithmic time lookup to locate neighbors. Internally uses
 * KDTree implementation of Weka libarary.<br>
 * 
 * 
 * @author Ankur Shanbhag
 *
 */
public class KdTrees implements NearestNeighbors {

    private ArrayList<Attribute> dimensionalAttributes = new ArrayList<>();
    private KDTree kdTree = new KDTree();

    @Override
    public void setup(List<DataObject> target, ConfigurationParams params) {

        if (null == target || target.isEmpty())
            throw new IllegalArgumentException("No datapoints found to contruct KDTree.");

        int numAttributes = target.get(0).getDimension();
        for (int i = 0; i < numAttributes; i++) {
            dimensionalAttributes.add(new Attribute(String.valueOf(i), i));
        }

        Instances targetTrain = new Instances("target", dimensionalAttributes, target.size());
        for (DataObject obj : target) {
            Instance instance = new DenseInstance(obj.getDimension());
            for (int i = 0; i < obj.getDimension(); i++) {
                instance.setValue(dimensionalAttributes.get(i), obj.getFeatures()[i]);
            }

            targetTrain.add(instance);
        }

        try {
            // construct KD-Tree
            kdTree.setInstances(targetTrain);
        } catch (Exception e) {
            throw new RuntimeException("Error while constructing KD-Tree.", e);
        }
    }

    @Override
    public Map<DataObject, Double> findNeighbors(DataObject obj, int K) {
        Instances predict = new Instances("source", dimensionalAttributes, 1);
        Instance instance = new DenseInstance(obj.getDimension());
        for (int i = 0; i < obj.getDimension(); i++) {
            instance.setValue(dimensionalAttributes.get(i), obj.getFeatures()[i]);
        }
        predict.add(instance);

        Map<DataObject, Double> map = new LinkedHashMap<>();

        try {
            Instances nearestInstances = kdTree.kNearestNeighbours(predict.firstInstance(), K);

            for (Instance neighbor : nearestInstances) {
                DataObject obj1 = new ImageData();
                double[] features = new double[dimensionalAttributes.size()];
                for (int i = 0; i < dimensionalAttributes.size(); i++) {
                    features[i] = neighbor.value(dimensionalAttributes.get(i));
                }
                obj1.setFeatures(features);
                map.put(obj1, DataTransformer.computeEuclideanDistance(obj, obj1));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error finding nearest neighbors using KD-Tree.", e);
        }
        return map;
    }
}

