package main;

import java.io.IOException;

import utils.Printer;
import algorithms.MFbpr;
import algorithms.ItemPopularity;
import algorithms.TopKRecommender;
import data_structure.Rating;

import java.util.ArrayList;

public class main_bpr extends main {
	public static void main(String argv[]) throws IOException {
		String dataset_name = "yelp";
		int factors = 16;
		double lr = 0.01;
		double reg = 0.01;
		int num_dns = 1; // number of dynamic negative samples [Zhang Weinan et al. SIGIR 2013]
		int maxIter = 1000;
		double init_mean = 0;
		double init_stdev = 0.01;
		
		if (argv.length > 0) {
			dataset_name = argv[0];
			factors = Integer.parseInt(argv[1]);
			lr = Double.parseDouble(argv[2]);
			reg = Double.parseDouble(argv[3]);
		}
		ReadRatings_HoldOneOut("data/" + dataset_name + ".rating");
		topK = 100;
		
		System.out.printf("BPR with factors=%d, lr=%.4f, reg=%.4f, num_dns=%d\n", 
				factors, lr, reg, num_dns);
		System.out.println("====================================================");
		
		ItemPopularity pop = new ItemPopularity(trainMatrix, testRatings, topK, threadNum);
		evaluate_model(pop, "Popularity");
		
		MFbpr bpr = new MFbpr(trainMatrix, testRatings, topK, threadNum, 
				factors, maxIter, lr, false, reg, init_mean, init_stdev, num_dns, true);
		evaluate_model(bpr, "BPR");
		
	} // end main
}
