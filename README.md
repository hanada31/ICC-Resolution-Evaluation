# Artifact for: A Comprehensive Evaluation of Android ICC Resolution Techniques

## Description
### Information about the paper.

This artifact is for paper： 
> Jiwei Yan, Shixin Zhang, Yepang Liu, Xi Deng, Jun Yan, Jian Zhang. [A Comprehensive Evaluation of Android ICC Resolution Techniques](ASE2022-A Comprehensive Evaluation of Android ICC Resolution Techniques.pdf), ASE 2022.

The contributions of this paper are threefold:

- We construct multiple-type benchmark suites for ICC resolution, which contain both hand-made apps designed with specific characteristics and real-world apps with complex ICC implementation, and propose a dynamic ICC extraction approach to obtain characteristic-labeled oracles for representative apps.

- We propose a unified ICC resolution comparison framework and design specific metrics for multiple-type benchmark suites.

- We carry out in-depth evaluations on six popular and state-of-the-art ICC resolution tools, clarify the strengths and weaknesses of each tool, summarize the root causes that lead to precision loss, and discuss the directions for further improvement.


### Description of this artifact.

In the corresponding paper, we made a comprehensive evaluation of Android ICC resolution techniques.

This artifact contains three parts:

- The information about multiple-type benchmark suites, including the hand-made benchmarks, the large-scale real-world benchmarks, and especially the reusable data for our characteristic-labeled ICC benchmark displayed on [ICCViewer](https://iccviewer.ldby.site/ICCViewer/). 
- The scripts and source files to generate the tables and figures used in the paper.
- The evaluation results on the three benchmark suites, including the execution time, the evaluation results of number-based and oracle-based metrics, and the number of FN ICCs with specific tags.



## Contents 

### BenchHand

The evaluation results on five handmade benchmarks, which contains 73 apps.

- **DroidBench**: the ICC resolution results of the six tools under evaluation, *A3E*, *IC3*, *IC3-DIALDroid*, *Gator*, *StoryDistiller* and *ICCBot*. For each tool, we give the graphical result of reported ICC in *appName_atg@toolName.pdf*, and the dot source file *appName_atg@toolName.dot*. For tools, e.g., *IC3* and *ICCBot*, that make extra analysis about the value of intent field, we give the corresponding reports provided by tools.

- **ICCBench**: Same as above.

- **RAICCBench**: Same as above.

- **StoryDroidBench**: Same as above.

- **ICCBotBench**: Same as above.

- **Statistic on BenchHand.xlsx**: statistic information, including the execution time, the evaluation results of number-based and oracle-based metrics, and the number of FN ICCs with specific tags. 

  

### BenchSmall

The evaluation results on BenchSmall, which contains 31 apps.

- **Data Set**: Information about apps.

- **Resolution Results**: the ICC resolution results of tools, A3E, IC3, IC3-DIALDroid, Gator, StoryDistiller and ICCBot. 

  - **DroidBench**: the ICC resolution results of the six tools under evaluation, *A3E*, *IC3*, *IC3-DIALDroid*, *Gator*, *StoryDistiller* and *ICCBot*. For each tool, we give the graphical result of reported ICC in *appName_atg@toolName.pdf*, and the dot source file *appName_atg@toolName.dot*. For tools, e.g., *IC3* and *ICCBot*, that make extra analysis about the value of intent field, we give the corresponding reports provided by tools.
  - **ICCBench**: Same as above.

  - **RAICCBench**: Same as above.

  - **StoryDroidBench**: Same as above.

  - **ICCBotBench**: Same as above.

- **Statistic on BenchSmall.xlsx**: statistic information, including the app information, execution time, the evaluation results of number-based, graph-based and oracle-based metrics, the number of FN ICCs with specific tags, and the pairwise comparison results.

  


### BenchLarge

The evaluation results on 2000 apps from f-droid, and 2000 ones from google play are given. 

- **Data Set:** Information about apps.

- **Resolution Results**: the ICC resolution results of tools, A3E, IC3, IC3-DIALDroid, Gator, StoryDistiller and ICCBot. 

  - Fdroid: *graphCount.txt* - statistic result using graph metrics; *numberCount.txt*- statistic result using numbermetrics; *oracleCount.txt*- statistic result using oracle metrics; *pairwiseCount.txt* - *pairwise comparison between any two tools; *tagResult.txt* - number of tags relates to the FN ICCs of each tool. 
  - Google Play: Same as above.

- **Statistic on BenchLarge.xlsx**: statistic information, including the execution time, the evaluation results of number-based and graph-based metrics.

  

### Figure Information

The drawing-related source files, which generate all the figures in the paper.

- drawPairwiseGraph.py. This is the source file of all the pairwise graph.

- source file of figures. This is the source file of all the tables and figures, except the pairwise graph, used in the paper, which is a pzfx format project and can be opened with *GraphPad Prism*.

  

### Tags and Patterns

Count the number of tags and ICCs relates to each pattern.

- **FN-ICC-tag-count:** Number of tags on common FN ICCs.
  - *158-FN-tags-count.json*
  - *Common FN ICCs and Patterns.xlsx*, including the number of ICCs missed in common, tags and patten analysis results of the ICCs missed in common.
- **All-ICC-tag-count:** Number of tags on all ICCs. 
  - *All-ICC-tag-count.json*.
- **other files:** ICCs relates to each pattern.
  - *number-patternName.json*



### ICCViewer

The ICCs in our oracle set and their corresponding tags, including the label information of 1,680 ICCs and the README file of  [ICCViewer](https://iccviewer.ldby.site/ICCViewer/). 

- Labels of ICCs:** Labels for the five benchmarks in BenchHand, and the 31 apps in BenchSmall.

- **README:** The [README](ICCViewer\README.md) file of  ICCViewer. 
- **Main UI of ICCViewer.**

<img src="Imgs\img1.png" alt="img1" style="zoom:67%;" />

- **The labeled ICC characteristics in ICCViewer.**

<img src="Imgs\img2.png" alt="img2" style="zoom:67%;" />

