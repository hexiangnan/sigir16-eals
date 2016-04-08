package main;

import java.io.IOException;

import data_structure.DenseMatrix;
import utils.Printer;
import algorithms.MF_fastALS;
import algorithms.MF_ALS;
import algorithms.MF_CD;
import algorithms.ItemPopularity;

public class main_MF extends main {
	public static void main(String argv[]) throws IOException {
		String dataset_name = "yelp";
		String method = "FastALS";
		double w0 = 10;
		boolean showProgress = false;
		boolean showLoss = true;
		int factors = 64;
		int maxIter = 500;
		double reg = 0.01;
		double alpha = 0.75;
		
		if (argv.length > 0) {
			dataset_name = argv[0];
			method = argv[1];
			w0 = Double.parseDouble(argv[2]);
			showProgress = Boolean.parseBoolean(argv[3]);
			showLoss = Boolean.parseBoolean(argv[4]);
			factors = Integer.parseInt(argv[5]);
			maxIter = Integer.parseInt(argv[6]);
			reg = Double.parseDouble(argv[7]);
			if (argv.length > 8) alpha = Double.parseDouble(argv[8]);
		}
		//ReadRatings_GlobalSplit("data/" + dataset_name + ".rating", 0.1);
		ReadRatings_HoldOneOut("data/" + dataset_name + ".rating");
		
		System.out.printf("%s: showProgress=%s, factors=%d, maxIter=%d, reg=%f, w0=%.2f, alpha=%.2f\n",
				method, showProgress, factors, maxIter, reg, w0, alpha);
		System.out.println("====================================================");
		
		ItemPopularity popularity = new ItemPopularity(trainMatrix, testRatings, topK, threadNum);
		evaluate_model(popularity, "Popularity");
		
		double init_mean = 0;
		double init_stdev = 0.01;
		
		if (method.equalsIgnoreCase("fastals")) {
			MF_fastALS fals = new MF_fastALS(trainMatrix, testRatings, topK, threadNum,
					factors, maxIter, w0, alpha, reg, init_mean, init_stdev, showProgress, showLoss);
			evaluate_model(fals, "MF_fastALS");
		}
		
		if (method.equalsIgnoreCase("als")) {
			MF_ALS als = new MF_ALS(trainMatrix, testRatings, topK, threadNum,
					factors, maxIter, w0, reg, init_mean, init_stdev, showProgress, showLoss);
			evaluate_model(als, "MF_ALS");
		}
		
		if (method.equalsIgnoreCase("cd")) {
			MF_CD cd = new MF_CD(trainMatrix, testRatings, topK, threadNum,
					factors, maxIter, w0, reg, init_mean, init_stdev, showProgress, showLoss);
			evaluate_model(cd, "MF_CD");
		}
		
		if (method.equalsIgnoreCase("all")) {
			DenseMatrix U = new DenseMatrix(userCount, factors);
			DenseMatrix V = new DenseMatrix(itemCount, factors);
			U.init(init_mean, init_stdev);
			V.init(init_mean, init_stdev);
			
			MF_fastALS fals = new MF_fastALS(trainMatrix, testRatings, topK, threadNum,
					factors, maxIter, w0, alpha, reg, init_mean, init_stdev, showProgress, showLoss);
			fals.setUV(U, V);
			evaluate_model(fals, "MF_fastALS");
			
			MF_ALS als = new MF_ALS(trainMatrix, testRatings, topK, threadNum,
					factors, maxIter, w0, reg, init_mean, init_stdev, showProgress, showLoss);
			als.setUV(U, V);
			evaluate_model(als, "MF_ALS");
			
			MF_CD cd = new MF_CD(trainMatrix, testRatings, topK, threadNum,
					factors, maxIter, w0, reg, init_mean, init_stdev, showProgress, showLoss);
			cd.setUV(U, V);
			evaluate_model(cd, "MF_CD");
		}
	
	} // end main
}
