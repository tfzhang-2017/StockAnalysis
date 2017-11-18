package com.stock.ztf.StockAnalysis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	public static List<String> readline(String file) {
		List<String> rets=new ArrayList<String>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String str;
			int line = 0;
			while ((str = br.readLine()) != null) {
				line++;
				if (!str.isEmpty() &&line>1) {
					rets.add(str.trim());
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rets;
	}
	
	public static List<String> readline(File file) {
		List<String> rets=new ArrayList<String>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String str;
			int line = 0;
			while ((str = br.readLine()) != null) {
				line++;
				if (!str.isEmpty() &&line>1) {
					rets.add(str.trim());
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rets;
	}
	
	public static List<String> readline(InputStreamReader file) {
		List<String> rets=new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(file);

			String str;
			int line = 0;
			while ((str = br.readLine()) != null) {
				line++;
				if (!str.isEmpty() &&line>1) {
					rets.add(str.trim());
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rets;
	}

	public static void main(String[] args) {
//		readline("D:\\Desktop\\Table.txt");
//		System.out.println("[-,1179.980,1130.112,-]".replaceAll("[^\\d\\.\\-,]", ""));
//		System.out.println(Float.parseFloat("-123.2"));
		System.out.println("123.45".replaceAll("\\.\\d+", ""));
	}

}
