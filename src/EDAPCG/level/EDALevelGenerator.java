package EDAPCG.level;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.clusterers.DensityBasedClusterer;
import weka.core.Instance;
import EDAPCG.auto.Executor;
import EDAPCG.cluster.Filters;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.level.Level;

//import org.apache.commons.math.stat.regression.*;

public class EDALevelGenerator implements LevelGenerator {

	public static String clusterFile = "src/EDAPCG/res/cluster.dat";

	protected static Random rand = new Random();
	protected EDALevel lvl;

	@Override
	public LevelInterface generateLevel(GamePlay playerMetrics) {

		// 320 blocks length for the level
		// start and end platform are 10 each
		// and chunks are 2 each
		// [320 - (10+10)] / 2 = 150
		// new order = 200 block in total, 10+(100*2)+10 = 220 (extra 20 can't
		// be accessed so)
		int tracelength = 100;

		int type = Level.TYPE_OVERGROUND; // default

		try {

			DensityBasedClusterer cl = readClusters(clusterFile);
			Instance inst = makeInstance(playerMetrics);
			double[] clusters = cl.logDensityPerClusterForInstance(inst);
			clusters = proportion(clusters);

			switch (weka.core.Utils.maxIndex(clusters)) {
			case 0: // explorer
				type = Level.TYPE_CASTLE;
				break;
			default: // speeder
				type = Level.TYPE_OVERGROUND;
				break;
			}

			Executor exec = new Executor(clusters);
			// other options:
			// - generateTraceExplorer
			// - generateTraceSpeeder
			// - generateTraceMix
			// (the first two mentioned above ignore odds;
			// the parameter passed in Executor constructor)
			Trace trace = exec.generateTracePhase(tracelength);
			// Trace trace = exec.generateTraceMix(tracelength);
			String str_LinCSV = ""; // these are Metric parameters
			String str_LenCSV = "";
			String str_PVCSV = "";
			String str_PDCSV = "";
			String str_DCSV = "";
			String str_AllFF ="";
			String str_EDAModel ="";
			// String str_PVDetail ="";
			String str_P_EDA ="";
			// arrays for various comparisons
			String[] EnemiesArr = { "GOOMBA", "REDTURTLE", "GREENTURTLE","SPIKY" };
			String[] FlatCoin = { "FLAT", "COINS" }; // to be added if needed
			String[] EnemyBlockArr = { "GOOMBA_BLOCK_PP", "GOOMBA_BLOCK_CC",
					"GOOMBA_BLOCK_EE", "GOOMBA_BLOCK_PC", "GOOMBA_BLOCK_PE",
					"GOOMBA_BLOCK_CE", "GREENTURTLE_BLOCK_PP",
					"GREENTURTLE_BLOCK_CC", "GREENTURTLE_BLOCK_EE",
					"GREENTURTLE_BLOCK_PC", "GREENTURTLE_BLOCK_PE",
					"GREENTURTLE_BLOCK_CE", "REDTURTLE_BLOCK_PP",
					"REDTURTLE_BLOCK_CC", "REDTURTLE_BLOCK_EE",
					"REDTURTLE_BLOCK_PC", "REDTURTLE_BLOCK_PE",
					"REDTURTLE_BLOCK_CE" };
			String[] BlockEnemyArr = { "BLOCK_PP_GOOMBA", "BLOCK_CC_GOOMBA",
					"BLOCK_EE_GOOMBA", "BLOCK_PC_GOOMBA", "BLOCK_PE_GOOMBA",
					"BLOCK_CE_GOOMBA", "BLOCK_PP_GREENTURTLE",
					"BLOCK_CC_GREENTURTLE", "BLOCK_EE_GREENTURTLE",
					"BLOCK_PC_GREENTURTLE", "BLOCK_PE_GREENTURTLE",
					"BLOCK_CE_GREENTURTLE", "BLOCK_PP_REDTURTLE",
					"BLOCK_CC_REDTURTLE", "BLOCK_EE_REDTURTLE",
					"BLOCK_PC_REDTURTLE", "BLOCK_PE_REDTURTLE",
					"BLOCK_CE_REDTURTLE" };
			String[] BlockArr = { "BLOCK_PP", "BLOCK_CC", "BLOCK_EE",
					"BLOCK_PC", "BLOCK_PE", "BLOCK_CE" };
			String[] WingedEnemiesArr = { "GOOMBA_WINGED", "REDTURTLE_WINGED",
					"GREENTURTLE_WINGED", "SPIKY_WINGED" };
			String[] AnyEnemyArr = { "GOOMBA_BLOCK_PP", "GOOMBA_BLOCK_CC",
					"GOOMBA_BLOCK_EE", "GOOMBA_BLOCK_PC", "GOOMBA_BLOCK_PE",
					"GOOMBA_BLOCK_CE", "GREENTURTLE_BLOCK_PP",
					"GREENTURTLE_BLOCK_CC", "GREENTURTLE_BLOCK_EE",
					"GREENTURTLE_BLOCK_PC", "GREENTURTLE_BLOCK_PE",
					"GREENTURTLE_BLOCK_CE", "REDTURTLE_BLOCK_PP",
					"REDTURTLE_BLOCK_CC", "REDTURTLE_BLOCK_EE",
					"REDTURTLE_BLOCK_PC", "REDTURTLE_BLOCK_PE",
					"REDTURTLE_BLOCK_CE", "GOOMBA", "REDTURTLE", "GREENTURTLE",
					"SPIKY", "BLOCK_PP_GOOMBA", "BLOCK_CC_GOOMBA",
					"BLOCK_EE_GOOMBA", "BLOCK_PC_GOOMBA", "BLOCK_PE_GOOMBA",
					"BLOCK_CE_GOOMBA", "BLOCK_PP_GREENTURTLE",
					"BLOCK_CC_GREENTURTLE", "BLOCK_EE_GREENTURTLE",
					"BLOCK_PC_GREENTURTLE", "BLOCK_PE_GREENTURTLE",
					"BLOCK_CE_GREENTURTLE", "BLOCK_PP_REDTURTLE",
					"BLOCK_CC_REDTURTLE", "BLOCK_EE_REDTURTLE",
					"BLOCK_PC_REDTURTLE", "BLOCK_PE_REDTURTLE",
					"BLOCK_CE_REDTURTLE", "GOOMBA_WINGED", "REDTURTLE_WINGED",
					"GREENTURTLE_WINGED", "SPIKY_WINGED" };
			double[][] FF = new double[7][100];
			double P_FLAT = 0, P_GAP = 0, P_GROUND_UP = 0, P_GROUND_DOWN = 0, P_STAIRS_UP = 0, P_STAIRS_DOWN = 0, P_BLOCK_CC = 0, P_GOOMBA = 0, P_GOOMBA_BLOCK_PP = 0, P_BLOCK_PP_GOOMBA = 0;
			// int stair_up_count = 0, stair_down_count = 0, Pillar_GAP_count = 0, enemy1_count = 0, enemy2_count = 0, enemy3_count = 0, enemy4_count = 0, roof_count = 0, gap_count = 0, multiple_GAP_count = 0, variable_GAP_count = 0, GAP_enemy_count = 0, valley_count = 0, Pipe_valley_count = 0, Empty_valley_count = 0, Enemy_valley_count = 0, Roof_valley_count = 0, Empty_stair_valley_count = 0, Enemy_stair_valley_count = 0, Gap_stair_valley_count = 0, two_path_count = 0, block_count = 0, Risk_and_Reward_count = 0; // Define
			// all
				// EDA Model parameters
			// a1 Linearity
			// a2 Leniency
			// a3 Pattern Density
			// a4 Pattern Variety
			// a5 Density
			double a1 = 0.1, a2 = 0.6, a3 = 0.1, a4 = 0.1, a5 = 0.1; //  values for F.F. coefficients 
			int individual = 100; // EDA generation pop
			int generations = 300; // EDA generation count
			double P_EDA = 0.0;
			for (int g = 1; g <= generations; g++) {
				if (g > 1)
					individual = 60; // after the second generation we create 60 traces to mix it up with the selected 40 from previous gen

				try {
					Path generations_path = Paths.get("D:\\Arash\\Generations\\G" + g); // reads form stored levels on disk
					Files.createDirectory(generations_path);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				for (int s = 0; s < individual; s++) // s Number of the file you
														// want to create (
														// Trace File )
				{
					// - generateTraceExplorer = higher weights
					// - generateTraceSpeeder  = lower weights
					// - generateTraceMix      = Mixture of two above
					// - generateTracePhase    = Based on player data
					
					if(g > 1)
					{
						if(P_EDA >=0.0 && P_EDA < 0.2)
							trace = exec.generateTraceSpeeder(tracelength);
						else if (P_EDA >=0.3 && P_EDA < 0.6)
							trace = exec.generateTraceMix(tracelength);
						else
							trace = exec.generateTracePhase(tracelength);
					}
					else {
						trace = exec.generateTraceMix(tracelength);
					}
					// P_EDA = 0.0;
					int min = 0; // linearity parameter
					int max = 0; // linearity parameter
					String str_chunk = ""; // linearity parameter
					String str_chunkHeight = ""; // linearity parameter
					int current = -1, tmpCurrent = 0; // linearity parameter
					double SumChunkHeight = 0.0;
					int stair_up_count = 0, stair_down_count = 0, Pillar_GAP_count = 0, enemy1_count = 0, enemy2_count = 0, enemy3_count = 0, enemy4_count = 0, roof_count = 0, gap_count = 0, multiple_GAP_count = 0, variable_GAP_count = 0, GAP_enemy_count = 0, valley_count = 0, Pipe_valley_count = 0, Empty_valley_count = 0, Enemy_valley_count = 0, Roof_valley_count = 0, Empty_stair_valley_count = 0, Enemy_stair_valley_count = 0, Gap_stair_valley_count = 0, two_path_count = 0, block_count = 0, Risk_and_Reward_count = 0; // Define
																																																																																																																																			// all
																																																																																																																																			// of
																																																																																																																																			// the
																																																																																																																																			// Pattern
																																																																																																																																			// Counter
																																																																																																																																			// Variable
																																																																																																																																			// here
					double stair_up_count_v = 0, stair_down_count_v = 0, Pillar_GAP_count_v = 0, enemy1_count_v = 0, enemy2_count_v = 0, enemy3_count_v = 0, enemy4_count_v = 0, roof_count_v = 0, gap_count_v = 0, multiple_GAP_count_v = 0, variable_GAP_count_v = 0, GAP_enemy_count_v = 0, valley_count_v = 0, Pipe_valley_count_v = 0, Empty_valley_count_v = 0, Enemy_valley_count_v = 0, Roof_valley_count_v = 0, Empty_stair_valley_count_v = 0, Enemy_stair_valley_count_v = 0, Gap_stair_valley_count_v = 0, two_path_count_v = 0, block_count_v = 0, Risk_and_Reward_count_v = 0; // Define
																																																																																																																																																// of
																																																																																																																																																// the
																																																																																																																																																// Pattern
																																																																																																																																																// Counter
																																																																																																																																																// Variable
																																																																																																																																																// here

					double Leniency = 0.0; 
					double ChunkHeight = 0.0; // leniency calculation
					double notEmptyPattern = 0.0;
					double PatternDensity = 0.0;
					double sum_of_v = 0.0;
					for (int i = 0; i < trace.size(); i++) {
						if (trace.getChunk(i).toString() == "GAP"
								&& i != trace.size() - 1) // if there is a gap
															// and it is not the
															// end tile of trace
						{
							if (trace.getChunk(i + 1).toString() == "PIPE") // after
																			// gap
																			// we
																			// look
																			// ahead
																			// for
																			// enemy
																			// tiles
							
								Leniency += 0.01; // simple gap - no enemies - gap length is always 2 tiles so weight is 0.01 
							// added gap length
							else if	((i + 1) < trace.size()
							&& (trace.getChunk(i + 1).toString() == "GAP"))
							{ // 2 GAPS
								Leniency += 0.75;
								if ((i + 3) < trace.size()
								&& (trace.getChunk(i + 2).toString() == "GAP"))
								{ // 3 GAPS
									Leniency += 0.75;
									if ((i + 3) < trace.size()
									&&(trace.getChunk(i + 3).toString() == "GAP")) // 4 GAPS
										Leniency += 0.5;								
								}
							}
							else if (trace.getChunk(i + 1).toString() == "PIPE_PIRANHA"
									|| Arrays.asList(BlockEnemyArr).contains(
											trace.getChunk(i).toString())
									|| Arrays.asList(EnemyBlockArr).contains(
											trace.getChunk(i).toString()))
								Leniency += 0.0; // gap with enemies in the
													// landing tile
							else
								Leniency += 0.01; // normal gap
						} else {
							if (Arrays.asList(BlockEnemyArr).contains(
									trace.getChunk(i).toString())
									|| Arrays.asList(EnemyBlockArr).contains(
											trace.getChunk(i).toString()))
								Leniency += 0.5; // chunk with enemy and neutral
													// element
							else if (trace.getChunk(i).toString() == "GOOMBA"
									|| trace.getChunk(i).toString() == "REDTURTLE"
									|| trace.getChunk(i).toString() == "GREENTURTLE"
									|| trace.getChunk(i).toString() == "SPIKY"
									|| trace.getChunk(i).toString() == "CANNON"
									|| trace.getChunk(i).toString() == "GOOMBA_WINGED"
									|| trace.getChunk(i).toString() == "REDTURTLE_WINGED"
									|| trace.getChunk(i).toString() == "GREENTURTLE_WINGED"
									|| trace.getChunk(i).toString() == "SPIKY_WINGED"
									|| trace.getChunk(i).toString() == "PIPE_PIRANHA")
								Leniency += 0.0; // enemy tile
							else
								Leniency += 1.0; // neutral tile
							FF[2][s] = Leniency / 100.0;
						}

						// pattern variation & pattern density calculations
						// for each pattern use if statement and if the
						// condition is true then ++ counter of that pattern
						if (trace.getChunk(i).toString() == "STAIRS_UP") // stair up pattern
						{stair_up_count++;} 
						
						else if (trace.getChunk(i).toString() == "STAIRS_DOWN") // stair down pattern
						{stair_down_count++;} 
						
						// enemy horde
						else if (Arrays.asList(EnemiesArr).contains(trace.getChunk(i).toString())) 
						{enemy1_count++;
						two_path_count++;} 
						
						 if ((i + 1) < trace.size()&&trace.getChunk(i).toString() == "GOOMBA"&&trace.getChunk(i+1).toString() == "GOOMBA")
								{enemy2_count++;} 
								
						else if ((i + 2) < trace.size()
								&& ((Arrays.asList(EnemiesArr).contains(trace.getChunk(i).toString()))
								&& (Arrays.asList(EnemiesArr).contains(trace.getChunk(i + 1).toString())) 
								&& (Arrays.asList(EnemiesArr).contains(trace.getChunk(i + 2).toString())))) 
								{enemy3_count++;} 
						
						 if ((i + 3) < trace.size()
								&& ((Arrays.asList(EnemiesArr).contains(trace.getChunk(i).toString()))
								&& (Arrays.asList(EnemiesArr).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(EnemiesArr).contains(trace.getChunk(i + 2).toString())) 
								&& (Arrays.asList(EnemiesArr).contains(trace.getChunk(i + 3).toString())))) 
								{enemy4_count++;}
								
							
						else if (
								(i + 2) < trace.size() &&
								((trace.getChunk(i).toString() == "GAP")
								||
								(trace.getChunk(i).toString() == "GAP" && trace.getChunk(i+1).toString() == "GAP")
								||
								(trace.getChunk(i).toString() == "GAP" && trace.getChunk(i+1).toString() == "GAP" && trace.getChunk(i+2).toString() == "GAP"))
								)
						{gap_count++;} 
					
						else if ((i + 4) < trace.size()
								&& (trace.getChunk(i).toString() == "GAP"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()) 
								&& trace.getChunk(i + 2).toString() == "GAP") ) 
								{multiple_GAP_count++;} 
						
						 if (
								
								((i + 4) < trace.size() )
								&& 
								((trace.getChunk(i).toString() == "GAP"
								&& trace.getChunk(i + 1).toString() == "GAP" && Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString())
								&& trace.getChunk(i + 3).toString() == "GAP" && trace.getChunk(i + 4).toString() == "GAP")
								|| 
								((i + 3) < trace.size() && trace.getChunk(i).toString() == "GAP" && trace.getChunk(i + 1).toString() == "GAP"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString()) && trace.getChunk(i + 3).toString() == "GAP")
								|| 
								((i + 3) < trace.size() && trace.getChunk(i).toString() == "GAP" && Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& trace.getChunk(i + 2).toString() == "GAP" && trace.getChunk(i + 3).toString() == "GAP")
								||
								((i + 5) < trace.size() && trace.getChunk(i).toString() == "GAP" && trace.getChunk(i + 1).toString() == "GAP"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString()) && Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())))
							) 
										{variable_GAP_count++;}

						 if ((i + 9) < trace.size()
								&& ((trace.getChunk(i).toString() == "GAP")
										&& (trace.getChunk(i + 1).toString() == "GAP")
										&& (trace.getChunk(i + 2).toString() == "GAP")
										&& (trace.getChunk(i + 3).toString() == "GAP")
										&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 4).toString()))
										&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 5).toString()))
										&& (trace.getChunk(i + 6).toString() == "GAP")
										&& (trace.getChunk(i + 7).toString() == "GAP")
										&& (trace.getChunk(i + 8).toString() == "GAP") 
										&& (trace.getChunk(i + 9).toString() == "GAP"))) 
										{variable_GAP_count++;
										variable_GAP_count++;} 
										
						 if ((i + 8) < trace.size()
								&& ((trace.getChunk(i).toString() == "GAP")
										&& (trace.getChunk(i + 1).toString() == "GAP")
										&& (trace.getChunk(i + 2).toString() == "GAP")
										&& (trace.getChunk(i + 3).toString() == "GAP")
										&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 4).toString()))
										&& (trace.getChunk(i + 5).toString() == "GAP")
										&& (trace.getChunk(i + 6).toString() == "GAP")
										&& (trace.getChunk(i + 7).toString() == "GAP") 
										&& (trace.getChunk(i + 8).toString() == "GAP"))) 
										{variable_GAP_count++;
										variable_GAP_count++;}

						 if ((i + 1) < trace.size()
								&& (((trace.getChunk(i).toString() == "GAP") 
								&& (Arrays.asList(WingedEnemiesArr).contains(trace.getChunk(i + 1).toString()))) 
								|| ((trace.getChunk(i).toString() == "GAP") 
								&& (Arrays.asList(BlockEnemyArr).contains(trace.getChunk(i + 1).toString()))))) 
								{GAP_enemy_count++;} 
								
						 if ((i + 2) < trace.size()
								&& (((trace.getChunk(i).toString() == "GAP")
								&& (trace.getChunk(i + 1).toString() == "PIPE") 
								&& (trace.getChunk(i + 2).toString() == "GAP")) 
								|| ((trace.getChunk(i).toString() == "GAP") 
								&& (Arrays.asList(BlockArr).contains(trace.getChunk(i + 1).toString()) 
								&& trace.getChunk(i + 2).toString() == "GAP")))) 
								{Pillar_GAP_count++;} 
							
						else if ((i + 4) < trace.size()
								&& ((trace.getChunk(i).toString() == "PIPE"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString())
								&& trace.getChunk(i + 3).toString() == "PIPE")	
								
								|| (trace.getChunk(i).toString() == "PIPE"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString())
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString()) 
								&& trace.getChunk(i + 4).toString() == "PIPE")
										
								|| (trace.getChunk(i).toString() == "STAIR_UP"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString())
								&& trace.getChunk(i + 3).toString() == "STAIR_DOWN")
								
								|| (trace.getChunk(i).toString() == "PIPE"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 2).toString())
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString()) 
								&& trace.getChunk(i + 4).toString() == "PIPE")
								
								|| (trace.getChunk(i).toString() == "STAIR_UP"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& trace.getChunk(i + 2).toString() == "COINS"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())
								&& trace.getChunk(i + 4).toString() == "STAIR_DOWN")
								
								|| (trace.getChunk(i).toString() == "STAIR_UP"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString())
								&& trace.getChunk(i + 2).toString() == "COINS"
								&& Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())
								&& trace.getChunk(i + 4).toString() == "STAIR_DOWN")))
								
							{valley_count++;
							Empty_valley_count++;
							Empty_stair_valley_count++;} 
							
							else if ((i + 4) < trace.size()
								&& ((trace.getChunk(i).toString() == "PIPE")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (trace.getChunk(i + 2).toString() == "PIPE_PIRANHA")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "PIPE"))) 
								
							{Pipe_valley_count++;
							valley_count++;} 
								
							else if ((i + 4) < trace.size()
								&& (((trace.getChunk(i).toString() == "PIPE")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(AnyEnemyArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "PIPE")) 
								|| 
								((trace.getChunk(i).toString() == "STAIR_UP")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(AnyEnemyArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "STAIR_DOWN")))) 
								
							{Enemy_valley_count++;
							valley_count++;
							Enemy_stair_valley_count++;
							Roof_valley_count++;} 
							
							else if ((i + 4) < trace.size()
								&& (((trace.getChunk(i).toString() == "PIPE")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(EnemyBlockArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "PIPE")) 
								|| ((trace.getChunk(i).toString() == "STAIR_UP")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(EnemyBlockArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "STAIR_DOWN"))
								|| ((trace.getChunk(i).toString() == "STAIR_UP")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(BlockArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "STAIR_DOWN"))
								|| ((trace.getChunk(i).toString() == "PIPE")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(BlockArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "PIPE")))) 
								
								{valley_count++;
								Roof_valley_count++;} 
								
							else if ((i + 4) < trace.size()
								&& ((trace.getChunk(i).toString() == "STAIR_UP")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (Arrays.asList(AnyEnemyArr).contains(trace.getChunk(i + 2).toString()))
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "STAIR_DOWN"))) 
								
								{valley_count++;
								Enemy_stair_valley_count++;} 
								
							 if ((i + 4) < trace.size()
								&& ((trace.getChunk(i).toString() == "STAIR_UP")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 1).toString()))
								&& (trace.getChunk(i + 1).toString() == "GAP")
								&& (Arrays.asList(FlatCoin).contains(trace.getChunk(i + 3).toString())) 
								&& (trace.getChunk(i + 4).toString() == "STAIR_DOWN"))) 
								
								{valley_count++;
								Gap_stair_valley_count++;} 
								
						else if (
								(Arrays.asList(EnemyBlockArr).contains(trace.getChunk(i).toString()))
								|| 
								(Arrays.asList(BlockEnemyArr).contains(trace.getChunk(i).toString()))
								) 
								
							{two_path_count++;
							roof_count++;}
							// block_count+=2;
							 
							
							 if (Arrays.asList(EnemyBlockArr).contains(trace.getChunk(i).toString()))
								{Risk_and_Reward_count++;}
						
						// end of the Pattern Section

						// Start of min and max of the level

						str_chunk += trace.getChunk(i).toString() + " "; // linearity
																			// calculation
						if (current == -1 
						// || trace.getChunk(i).toString() == "GAP")
						&& (trace.getChunk(i).toString() == "GAP" || trace.getChunk(i).toString() == "GROUND_DOWN"))
							current = current + 0;
						if (current < 7 && trace.getChunk(i).toString() == "GROUND_UP") 
							current = current + 1;
						
						if ("GROUND_DOWN" == trace.getChunk(i).toString())
							current = current - 1;

						ChunkHeight = ((current + 1.0) * 2.0) / 15.0;
						
					//	str_chunkHeight += Double.toString(ChunkHeight) + "\n";

						SumChunkHeight += Math.abs(ChunkHeight); // no negative value
						if (SumChunkHeight >=100)
							FF[3][s] = 1;
						else 
							FF[3][s] = (SumChunkHeight / 100.0); 
						

						System.out.print(trace.getChunk(i) + "\n");
					}

					stair_up_count_v = (stair_up_count);
					stair_down_count_v = (stair_down_count);
					Pillar_GAP_count_v = (Pillar_GAP_count * 3);
					enemy1_count_v = (enemy1_count);
					enemy2_count_v = (enemy2_count * 2);
					enemy3_count_v = (enemy3_count * 3);
					enemy4_count_v = (enemy4_count * 4);
					roof_count_v = (roof_count * 1);
					gap_count_v = (gap_count * 1);
					multiple_GAP_count_v = (multiple_GAP_count * 4);
					variable_GAP_count_v = (variable_GAP_count * 4);
					GAP_enemy_count_v = (GAP_enemy_count * 2);
					valley_count_v = (valley_count * 5);
					Pipe_valley_count_v = (Pipe_valley_count * 5);
					Empty_valley_count_v = (Empty_valley_count * 5);
					Enemy_valley_count_v = (Enemy_valley_count * 5);
					Roof_valley_count_v = (Roof_valley_count * 5);
					Empty_stair_valley_count_v = (Empty_stair_valley_count * 5);
					Enemy_stair_valley_count_v = (Enemy_stair_valley_count * 5);
					Gap_stair_valley_count_v = (Gap_stair_valley_count * 5);
					two_path_count_v = (two_path_count);
					// block_count_v = (block_count); // /200.0;
					Risk_and_Reward_count_v = (Risk_and_Reward_count);

					FF[6][s] = two_path_count / 100.0; // two path is density metric

					if (stair_up_count > 0)
						notEmptyPattern++;
					if (stair_down_count > 0)
						notEmptyPattern++;
					if (Pillar_GAP_count > 0)
						notEmptyPattern++;
					if (enemy1_count > 0)
						notEmptyPattern++;
					if (enemy2_count > 0)
						notEmptyPattern++;
					if (enemy3_count > 0)
						notEmptyPattern++;
					if (enemy4_count > 0)
						notEmptyPattern++;
					if (roof_count > 0)
						notEmptyPattern++;
					if (gap_count > 0)
						notEmptyPattern++;
					if (multiple_GAP_count > 0)
						notEmptyPattern++;
					if (variable_GAP_count > 0)
						notEmptyPattern++;
					if (GAP_enemy_count > 0)
						notEmptyPattern++;
					if (valley_count > 0)
						notEmptyPattern++;
					if (Pipe_valley_count > 0)
						notEmptyPattern++;
					if (Empty_valley_count > 0)
						notEmptyPattern++;
					if (Enemy_valley_count > 0)
						notEmptyPattern++;
					if (Roof_valley_count > 0)
						notEmptyPattern++;
					if (Empty_stair_valley_count > 0)
						notEmptyPattern++;
					if (Enemy_stair_valley_count > 0)
						notEmptyPattern++;
					if (Gap_stair_valley_count > 0)
						notEmptyPattern++;
					if (two_path_count > 0)
						notEmptyPattern++;
					// if (block_count_v > 0)
					//	notEmptyPattern++;
					if (Risk_and_Reward_count > 0)
						notEmptyPattern++;
					
					
					

					FF[4][s] = notEmptyPattern / 22.0; // Pattern Variety

					sum_of_v = 
							stair_up_count_v + stair_down_count_v
							+ enemy2_count_v
							+ roof_count_v 
							+ gap_count_v 
							+ valley_count_v 
							//+ Pipe_valley_count_v + Empty_valley_count_v + Enemy_valley_count_v + Roof_valley_count_v + Empty_stair_valley_count_v + Enemy_stair_valley_count_v + Gap_stair_valley_count_v + variable_GAP_count_v
							//+ block_count_v + Risk_and_Reward_count_v + two_path_count_v + Pillar_GAP_count_v + enemy1_count_v + enemy3_count_v + enemy4_count_v + GAP_enemy_count_v + multiple_GAP_count_v
							;
					PatternDensity = sum_of_v / 100.0;
					FF[5][s] = PatternDensity;
					// Start Calculating FF
					// a1 += ((0.2 * P_FLAT) + (0.6 * P_GROUND_UP) + (0.2 * P_GROUND_DOWN));
					// a2 += ((0.5 * P_GAP) + (0.5 * P_GOOMBA));
					// a3 += ((0.3 * P_STAIRS_UP) + (0.3 * P_STAIRS_DOWN) + (0.4 * P_GAP));
					// a4 += ((0.3 * P_STAIRS_UP) + (0.3 * P_STAIRS_DOWN) + (0.2 * P_GAP) + (0.2 * P_GOOMBA));
					// a5 += ((0.4 * P_BLOCK_CC) + (0.3 * P_GOOMBA_BLOCK_PP) + (0.3 * P_BLOCK_PP_GOOMBA));
					FF[0][s] = ((a1 * (SumChunkHeight / 100.0)) + (a2 * (Leniency / 100.0))
							+ (a3 * PatternDensity)
							+ (a4 * (notEmptyPattern / 22.0))
							+ (a5 * (two_path_count_v / 100.0)));
					FF[1][s] = s; // Store index of individual in population

					System.out.println("P_GAP : " + P_GAP); //  in this command we can send specific parameter value to the output stream
					System.out.println("P_FLAT : " + P_FLAT);
					System.out.println("P_GROUND_UP : " + P_GROUND_UP);
					System.out.println("P_GROUND_DOWN : " + P_GROUND_DOWN);
					System.out.println("P_STAIRS_UP : " + P_STAIRS_UP);
					System.out.println("P_STAIRS_DOWN : " + P_STAIRS_DOWN); 
					System.out.println("notEmptyPattern : " + notEmptyPattern);
					System.out.println("sum_of_v : "        + sum_of_v);
					System.out.println("SumChunkHeight : " + SumChunkHeight);
					
					

//					try {
//						FileWriter fw = new FileWriter(
//								"D:\\Arash\\Level_Leniency\\" + s
//										+ "Leniency.txt");
//						fw.write(Double.toString(Leniency / 100.0));
//						fw.close();
//					} catch (Exception e) {
//						System.out.println(e);
//					}
					// str_chunkHeight += "----------------------------------\n";
					// str_chunkHeight += "Sum of Chunk Height = "
					// 		+ Double.toString(SumChunkHeight) + "\n";
					// str_chunkHeight += "Avg of Chunk Height = "
					// 		+ Double.toString(1.0 - (SumChunkHeight / 100.0))
					// 		+ "\n";
					// str_chunkHeight += "Min of Chunk Height = "
					// 		+ Double.toString(min + 1) + "\n";
					// str_chunkHeight += "Max of Chunk Height = "
					// 		+ Double.toString(max + 1) + "\n";
					// System.out.print("Min : " + min);
					// System.out.print("Max : " + max);
					try {
						FileWriter fw2 = new FileWriter("D:\\Arash\\Generations\\G"+(g)+ "\\Level_"
								+ s + ".txt");
						fw2.write(str_chunk);
						fw2.close();
					} catch (Exception e) {
						System.out.println(e);
					}
//					str_LenCSV += "Level " + s + ","
//							+ Double.toString(Leniency / 100.0) + "\n";
//					str_LinCSV += "Level " + s + ","
//							+ Double.toString(1.0 - (SumChunkHeight / 100.0))
//							+ "\n";
//					str_PVCSV += "Level " + s + ","
//							+ Double.toString(notEmptyPattern / 23.0) + "\n";
//					str_PDCSV += "Level " + s + ","
//							+ Double.toString(PatternDensity) + "\n";

//					try {
//						FileWriter fw3 = new FileWriter(
//								"D:\\Arash\\Level_Linearity\\" + s + ".txt");
//						fw3.write(str_chunkHeight);
//						fw3.close();
//					} catch (Exception e) {
//						System.out.println(e);
//					}
					//System.out.println("Success...");
				} // end individual for => Level Generation
					// Sort Individual By FF
					// index 0 is FF
					// index 1 is the index of individual
					// index 2 is Leniency value of individual -  1=Lenient,    0=nonLenient
					// index 3 is Linearity value of individual - 1=Linear,     0=nonLinear
					// index 4 is Pattern variety value
					// index 5 is Pattern density
					// index 6 is Density value
				double[][] sorted_ff = new double[7][100]; // comparison and sort in the FF array
				sorted_ff[0][0] = FF[0][0];
				sorted_ff[1][0] = FF[1][0];
				sorted_ff[2][0] = FF[2][0];
				sorted_ff[3][0] = FF[3][0];
				sorted_ff[4][0] = FF[4][0];
				sorted_ff[5][0] = FF[5][0];
				sorted_ff[6][0] = FF[6][0];
				for (int z = 1; z < 100; z++) {
					int q = z;
					if (FF[0][z] < sorted_ff[0][z - 1]) {
						for (; q >= 0; q--) {
							if (q == 0) {
								sorted_ff[0][q] = FF[0][z];
								sorted_ff[1][q] = FF[1][z];
								sorted_ff[2][q] = FF[2][z];
								sorted_ff[3][q] = FF[3][z];
								sorted_ff[4][q] = FF[4][z];
								sorted_ff[5][q] = FF[5][z];
								sorted_ff[6][q] = FF[6][z];
								break;
							}
							if (FF[0][z] >= sorted_ff[0][q - 1]) {
								sorted_ff[0][q] = FF[0][z];
								sorted_ff[1][q] = FF[1][z];
								sorted_ff[2][q] = FF[2][z];
								sorted_ff[3][q] = FF[3][z];
								sorted_ff[4][q] = FF[4][z];
								sorted_ff[5][q] = FF[5][z];
								sorted_ff[6][q] = FF[6][z];
								break;
							} else {
								sorted_ff[0][q] = sorted_ff[0][q - 1];
								sorted_ff[1][q] = sorted_ff[1][q - 1];
								sorted_ff[2][q] = sorted_ff[2][q - 1];
								sorted_ff[3][q] = sorted_ff[3][q - 1];
								sorted_ff[4][q] = sorted_ff[4][q - 1];
								sorted_ff[5][q] = sorted_ff[5][q - 1];
								sorted_ff[6][q] = sorted_ff[6][q - 1];
							}
						}

					} else {
						sorted_ff[0][z] = FF[0][z];
						sorted_ff[1][z] = FF[1][z];
						sorted_ff[2][z] = FF[2][z];
						sorted_ff[3][z] = FF[3][z];
						sorted_ff[4][z] = FF[4][z];
						sorted_ff[5][z] = FF[5][z];
						sorted_ff[6][z] = FF[6][z];
					}

				}
				FF = sorted_ff;
				if (g > 1) {
					for (int zz = 60; zz < 100; zz++) {
						File source = new File("D:\\Arash\\Generations\\G"+(g-1)+ "\\Level_" + (int) sorted_ff[1][zz] + ".txt");
					    					  // write to file output section
						File dest = new File("D:\\Arash\\Generations\\G"+(g)+ "\\Level_" + zz + ".txt");
						Files.copy(source.toPath(), dest.toPath(),
								StandardCopyOption.REPLACE_EXISTING);
						
						String Current_Level = "";
					    try {
					    	Current_Level = new String(Files.readAllBytes(Paths.get("D:\\Arash\\Generations\\G"+(g-1)+ "\\Level_" + (int) sorted_ff[1][zz] + ".txt")));
					    } catch (Exception e) {
					      e.printStackTrace();
					    }
					    String[] elements = Current_Level.split(" ");
					    for(int aa= 0;aa<100;aa++)
					    {
					    	if(elements[aa].equals("FLAT"))
					    		P_FLAT++;
					    	if(elements[aa].equals("GAP"))
					    		P_GAP++;
					    	if(elements[aa].equals("GROUND_UP"))
					    		P_GROUND_UP++;
					    	if(elements[aa].equals("GROUND_DOWN"))
					    		P_GROUND_DOWN++;
					    	if(elements[aa].equals("STAIRS_UP"))
					    		P_STAIRS_UP++;
					    	if(elements[aa].equals("STAIRS_DOWN"))
					    		P_STAIRS_DOWN++;
					    	if(elements[aa].equals("BLOCK_CC"))
					    		P_BLOCK_CC++;
							if(elements[aa].equals("GOOMBA"))
					    		P_GOOMBA++;
						   	if(elements[aa].equals("GOOMBA_BLOCK_PP"))
						   		P_GOOMBA_BLOCK_PP++;
						   	if(elements[aa].equals("P_BLOCK_PP_GOOMBA"))
						   		P_BLOCK_PP_GOOMBA++;
					    }
					    	// EDA MODEL par calculations
				    		P_GAP /=100.0;
				    		P_FLAT /=100.0;
				    		P_GROUND_UP /=100.0;
				    		P_GROUND_DOWN /=100.0;
				    		P_STAIRS_UP /=100.0;
				    		P_STAIRS_DOWN /=100.0;
				    		P_BLOCK_CC /=100.0;
				    		P_GOOMBA /=100.0;
					   		P_GOOMBA_BLOCK_PP /=100.0;
					   		P_BLOCK_PP_GOOMBA /=100.0;
					   		P_EDA = P_GAP + P_FLAT + P_GROUND_UP + P_STAIRS_UP + P_BLOCK_CC + P_GOOMBA + P_GOOMBA_BLOCK_PP + P_BLOCK_PP_GOOMBA;  // AVG of Ps
						str_EDAModel += "Generation " + g + " Level "+zz+"," // write to file
								+Double.toString(P_GAP)+","+Double.toString(P_FLAT)+","+Double.toString(P_GROUND_UP)+","+Double.toString(P_GROUND_DOWN)+","+Double.toString(P_STAIRS_UP)+","+Double.toString(P_STAIRS_DOWN)+","+Double.toString(P_BLOCK_CC)+","+Double.toString(P_GOOMBA)+"\n"; // ","+Double.toString(P_GOOMBA_BLOCK_PP)+","+Double.toString(P_BLOCK_PP_GOOMBA)+
							// P_FLAT = 0.0; P_GAP = 0.0; P_GROUND_UP = 0.0; P_GROUND_DOWN = 0.0; P_STAIRS_UP = 0.0; P_STAIRS_DOWN = 0.0; P_BLOCK_CC = 0.0; P_GOOMBA = 0.0; P_GOOMBA_BLOCK_PP = 0.0; P_BLOCK_PP_GOOMBA = 0.0;																																																																																																																																											// all
						
						 ///str_PVDetail += "Generation " + g + " Level "+zz+"," +Double.toString(stair_up_count)+ "," +Double.toString(stair_down_count) + "," +Double.toString(Pillar_GAP_count)+ "," +Double.toString(enemy1_count)+ "," +Double.toString(enemy2_count)
						// 		+ "," +Double.toString(enemy3_count)+ "," +Double.toString(enemy4_count)+ "," +Double.toString(roof_count)+ "," +Double.toString(gap_count)+ "," +Double.toString(multiple_GAP_count)+ "," +Double.toString(variable_GAP_count)
						 //		+ "," +Double.toString(GAP_enemy_count)+ "," +Double.toString(valley_count)+ "," +Double.toString(Pipe_valley_count)+ "," +Double.toString(Empty_valley_count)+ "," +Double.toString(Enemy_valley_count)+ "," +Double.toString(Roof_valley_count)
						///		+ "," +Double.toString(Empty_stair_valley_count)+ "," +Double.toString(Enemy_stair_valley_count)+ "," +Double.toString(Gap_stair_valley_count)+ "," +Double.toString(two_path_count)+ "," +Double.toString(Risk_and_Reward_count)+"\n";
					}
				}

				File source = new File("D:\\Arash\\Generations\\G"+(g)+ "\\Level_" + (int) sorted_ff[1][99] + ".txt");
				File dest = new File("D:\\Arash\\Levels\\" + g + ".txt");

				Files.copy(source.toPath(), dest.toPath(),
						StandardCopyOption.REPLACE_EXISTING);

			
				// commented section writes the full report for all of the individuals
				//for(int ss = 0; ss < 100; ss++) 
				//{
				//	str_AllFF += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(sorted_ff[0][ss]) + "\n";
				//	str_LenCSV += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(sorted_ff[2][ss]) + "\n";
				//	str_LinCSV += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(1-(sorted_ff[3][ss]))
				//			+ "\n";
				//	str_PVCSV += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(sorted_ff[4][ss]) + "\n";
				//	str_PDCSV += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(sorted_ff[5][ss]) + "\n";
				//	str_DCSV += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(sorted_ff[6][ss]) + "\n";
				//	str_P_EDA += "Generation " + g + " Level "+ss+","
				//			+ Double.toString(P_EDA) + "\n";
				//}
			for(int ss = 0; ss < 100; ss++) 
				{
					str_AllFF += "Generation " + g + " Level "+ss+","
							+ Double.toString(sorted_ff[0][ss]) + "\n";
					str_LenCSV += "Generation " + g + " Level "+ss+","
							+ Double.toString(sorted_ff[2][ss]) + "\n";
					str_LinCSV += "Generation " + g + " Level "+ss+","
							+ Double.toString(1-(sorted_ff[3][ss]))
							+ "\n";
					str_PVCSV += "Generation " + g + " Level "+ss+","
							+ Double.toString(sorted_ff[4][ss]) + "\n";
					str_PDCSV += "Generation " + g + " Level "+ss+","
							+ Double.toString(sorted_ff[5][ss]) + "\n";
					str_DCSV += "Generation " + g + " Level "+ss+","
							+ Double.toString(sorted_ff[6][ss]) + "\n";
					str_P_EDA += "Generation " + g + " Level "+ss+","
							+ Double.toString(P_EDA) + "\n";
				
				}
				// file writers

				try {
					FileWriter fw3 = new FileWriter("D:\\Arash\\Len.csv");
					fw3.write(str_LenCSV);
					fw3.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw10 = new FileWriter("D:\\Arash\\P_EDA.csv");
					fw10.write(str_P_EDA);
					fw10.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw3 = new FileWriter("D:\\Arash\\Lin.csv");
					fw3.write(str_LinCSV);
					fw3.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw4 = new FileWriter("D:\\Arash\\PV.csv");
					fw4.write(str_PVCSV);
					fw4.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw5 = new FileWriter("D:\\Arash\\PD.csv");
					fw5.write(str_PDCSV);
					fw5.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw6 = new FileWriter("D:\\Arash\\D.csv");
					fw6.write(str_DCSV);
					fw6.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw7 = new FileWriter("D:\\Arash\\FF.csv");
					fw7.write(str_AllFF);
					fw7.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				try {
					FileWriter fw8 = new FileWriter("D:\\Arash\\EDA_Model.csv");
					fw8.write(str_EDAModel);
					fw8.close();
				} catch (Exception e) {
					System.out.println(e);
				}
				// try {
				//	FileWriter fw11 = new FileWriter("D:\\Arash\\PV_Detail.csv");
				//	fw11.write(str_PVDetail);
				//	fw11.close();
				// } catch (Exception e) {
				//	System.out.println(e);
				// }
			} // End of Generation Creation
			String str_cc = "";
			for( int gg = 1; gg <= generations ; gg++ )
			{
				for( int gg_c = 1; gg_c <= generations ;gg_c++ )
				{	
					String Current_Level = "";
					try {
						Current_Level = new String(Files.readAllBytes(Paths.get("D:\\Arash\\Levels\\" + (gg) + ".txt")));
					} catch (Exception e) {
					  e.printStackTrace();
					}
					String[] elements = Current_Level.split(" ");
					
					
					String Compare_Level = "";
					try {
						Compare_Level = new String(Files.readAllBytes(Paths.get("D:\\Arash\\Levels\\" + (gg_c) + ".txt")));
					} catch (Exception e) {
					  e.printStackTrace();
					}
					String[] elements_c = Compare_Level.split(" ");
					double cc = 0;
					for( int xxx = 0; xxx<100 ; xxx++)
					{
						//System.out.println(elements[xxx]);
						if(elements[xxx].equals(elements_c[xxx]))
							cc ++;
					}
					double cc_avg = cc/100.0;
					if(gg_c>= gg)
						str_cc +=   cc_avg + ",";
					else
						str_cc +=  " ,";

							
				}
				str_cc += "\n";

			}
			try {
				FileWriter fw9 = new FileWriter("D:\\Arash\\EDA_NCD.csv");
				fw9.write(str_cc);
				fw9.close();
			} catch (Exception e) {
				System.out.println(e);
			}

			EDALevel lvl = trace.buildLevel(type);

			if (null == lvl)
				throw new Exception("Error while building level from genes.");

			return lvl;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		return null;
	}

	public DensityBasedClusterer readClusters(String fileName) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		DensityBasedClusterer cl = null;
		try {
			fis = new FileInputStream(fileName);
			in = new ObjectInputStream(fis);
			cl = (DensityBasedClusterer) in.readObject();
			fis.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cl;
	}

	protected Instance makeInstance(GamePlay gp) {
		try {

			Double d = 0.0;
			int k = 0;
			Field[] flds = GamePlay.class.getFields();
			Instance inst = new Instance(flds.length
					- Filters.numSkippedFields());
			Arrays.sort(flds, new Filters().new FieldComparator());

			for (int i = 0; i < flds.length; i++) {

				if (Filters.isSkippedField(flds[i].getName()))
					continue;

				if (flds[i].getType() == Integer.TYPE)
					d = new Double((Integer) flds[i].get(gp));
				else if (flds[i].getType() == Double.TYPE)
					d = (Double) flds[i].get(gp);

				inst.setValue(k, d);
				k++;
			}

			return inst;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	protected double[] proportion(double[] odds) {

		// ok this merits some explanation
		// we'll receive numbers like -87 and -120
		// higher number means higher probability
		// so what we do is negate them
		// and swap them
		// then act like they are weights

		double[] _odds = new double[odds.length];
		double accum = 0.0, total = 0.0;

		_odds[0] = -odds[1];
		_odds[1] = -odds[0];

		for (double d : _odds)
			total += d;
		for (int i = 0; i < _odds.length; i++) {
			accum += _odds[i];
			_odds[i] = accum / total;
		}

		return _odds;
	}

}
