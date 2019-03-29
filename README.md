# LT Expertfinder
This is the repository of LT Expertfinder, an open source evaluation framework for expert finding methods. The LT Expertfinder enables you to compare, evaluate and use different expert finding methods. To leverage the evaluation process, LT Expertfinder provides many different tools that will help to a obtain detailed insights into the expert finding methods. 

Currently, the tool operates on the ACL Anthology Network (http://tangra.cs.yale.edu/newaan/). However, by setting up your own server, you can exchange the data as you wish. Also, we are currently working on expanding the dataset with the papers from arXiv (https://arxiv.org/).

A running version of this tool can be found at http://ltdemos.informatik.uni-hamburg.de/lt-expertfinder/ui.

A short demonstration video is also availale at https://youtu.be/A4yRZezWUvE.

The demonstration paper "LT Expertfinder: An Evaluation Frameworkfor Expert Finding Methods" will be published in NAACL 2019 and can be viewd at <Link>.
 
 ## Expert Finding Methods
 The LT Expertfinder ships already with some basic expert finding methods:
 - Model2 by Balog et. al.
 - K-step Random Walk by Serdyukov et. al.
 - Infinite Random Walk by Serdyukov et. al.
 - Weighted Relevance Propagation
 - PageRank
 - H-Index Ranking (simple baseline method)
 - Citation Ranking (simple baseline method)
 
 If you wish to add your own expert finding method and use the tool to compare it see the section below.
 
 ## Set up your own LT Expertfinder
 While the LT Expertfinder is already a good tool to use, compare and evaluate our pre-implemented expert finding methods, you might want to add to these methods by setting up your own instance of LT Expertfinder and expanding the source code. Implementing your own expert finding method will enable you to compare it to the already existing methods. If you are intereseted in this, follow the steps below, otherwise, if you just want to use or the existing methods we recommend you to use the running version of this tool.
