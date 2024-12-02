library(extRemes)
library(evd)
library("Cairo")
library(ggplot2)
library(MASS)

pos_shape <- FALSE
if(grepl("False", namefile, fixed = TRUE)){
  pos_shape <- TRUE
}  

namefile <- "./Subjects/blazer_gpt14_unsafe/log/log_30min_1/output.csv"
log_file <- "./Subjects/blazer_gpt14_unsafe/log/log_30min_1/Log.txt"

a <- read.csv(namefile)

a_complete <- read.csv(log_file)
a_complete <- a_complete[,1]

# The column to use (roghly ((k/2)-1)*5 of Exponentiality Test)
k <- 8

myColumns <- a[, c(k)]

myColumns <- na.omit(myColumns)

aa <- as.vector(myColumns)

aa <- sample(aa, length(aa))

# mean residual plot
# mrlplot(a)
# t_approx <- quantile(aa, c(.9,.95, .997))

# Range between shape and scale from the Boot_strap_Threshold
for (t in seq(288.07, 2594, by=+0.5)){
  final = paste("./Figures/threshold_",namefile, ".png",sep = '')
  Cairo(file=final,
        bg="white",
        type="png",
        units="in",
        width=12,
        height=9,
        pointsize=14,
        dpi=100)
  par(mar=c(7, 6, 2,2) + 0.2)
  
  plot(aa, type = "p", col = "blue", lwd = 1, cex.lab = 2, cex.axis = 2, font = 2, xlab = "Samples", ylab = "Cost")
  abline(h = t, col = "red", lty=c(2), lwd=c(2))
  points(which(aa > t, arr.ind = TRUE), aa[aa > t], col="red")
  dev.off()
  
  start_time <- as.numeric(Sys.time())*1000
  # Bayesian approach
  # fit <- fevd(aa, type = "PP", method = "Bayesian", threshold = t, time.units = "days", units = "Time", period.basis = "1 unit = 365 sample")
  
  # GP or PP or Exponential
  type_name = "GP"
  fit <- try(fevd(aa, type = type_name, threshold = t, time.units = "days", units = "Time", period.basis = "1 unit = 365 sample"), silent = TRUE)
  
  try(distill(fit), silent = TRUE)
  
  end_time <- as.numeric(Sys.time())*1000
  time2 <- end_time - start_time
  
  # final = paste("./Figures/rl_",namefile, "_", ".png",sep = '')
  # Cairo(file=final,
  #       bg="white",
  #       type="png",
  #       units="in",
  #       width=12,
  #       height=9,
  #       pointsize=14,
  #       dpi=100)
  # par(mar=c(7, 6, 2,2) + 0.2)
  # 
  # plot(fit, "rl", rperiods = c(3, 6, 9, 15, 30, 45, 60), col = "blue",
  #      main = paste("Return Values"), lwd = 2, cex.axis = 1, cex.lab = 2, font = 2)
  # dev.off()
  # 
  # 
  # final = paste("./Figures/qq_",namefile, ".png",sep = '')
  # Cairo(file=final,
  #       bg="white",
  #       type="png",
  #       units="in",
  #       width=12,
  #       height=9,
  #       pointsize=14,
  #       dpi=100)
  # par(mar=c(7, 6, 2,2) + 0.2)
  # 
  # plot(fit, "qq",main = paste("Empirical quantiles against model quantiles"), col = "blue", lwd = 2, cex.axis = 1, cex.lab = 2, font = 2)
  # 
  # dev.off()
  # 
  # fit$period.basis <- "" 
  # 
  # final = paste("./Figures/density_",namefile, ".png",sep = '')
  # Cairo(file=final,
  #       bg="white",
  #       type="png",
  #       units="in",
  #       width=12,
  #       height=9,
  #       pointsize=14,
  #       dpi=100)
  # par(mar=c(7, 6, 2,2) + 0.2)
  # 
  # plot(fit, "density", sub = "", main = "", xlab = "Execution Times", col=c("black","blue"), lwd = 2, cex.axis = 1, cex.lab = 2, font = 2)
  # dev.off()
  
  
  fit$period.basis <- "1 unit = 365 sample" 
  
  gauss <- fitdistr(aa, "normal")
  
  # next 1,000; 2,000, 5,000, 10,000, 20,000, and 50,000 samples!
  summary(fit)

  print(paste("threshold_0 is : ", t))
  print(paste("num. items over threshold t : ", length(aa[aa > t])))
  print(paste("computation time", time2))
  print(paste("The mean and std=:", gauss$estimate))
  print(paste("The error for mean and std:", gauss$sd))
  print(paste("the length of training is ", length(aa)))
  print(paste("the max element in training is ", max(aa)))
  print(paste("The length of testing set is: ", length(a_complete) - length(aa)))
  print(paste("The overall max is :", max(a_complete)))
  print(paste("The index of overall max is :", which.max(a_complete)))
  
  try(return.level(fit), silent = TRUE)
  try(return.level(fit, do.ci=FALSE), silent = TRUE)
  num_remaining_elements <- length(a_complete) - length(aa)
  num_remaining_elements_years <- num_remaining_elements/365.0
  if(num_remaining_elements_years < 2.73972602739726){
    returns <- try(ci(fit, return.period=c(num_remaining_elements_years, 2.73972602739726, 2*2.73972602739726, 5* 2.73972602739726, 10 * 2.73972602739726, 20 * 2.73972602739726, 50*2.73972602739726)), silent = TRUE)
  }
  else if(num_remaining_elements_years < 2* 2.73972602739726)
  {
    returns <- try(ci(fit, return.period=c(2.73972602739726, num_remaining_elements_years, 2*2.73972602739726, 5* 2.73972602739726, 10 * 2.73972602739726, 20 * 2.73972602739726, 50*2.73972602739726)), silent = TRUE)
  }
  else if(num_remaining_elements_years < 5* 2.73972602739726)
  {
    returns <- try(ci(fit, return.period=c(2.73972602739726, 2*2.73972602739726, num_remaining_elements_years, 5* 2.73972602739726, 10 * 2.73972602739726, 20 * 2.73972602739726, 50*2.73972602739726)), silent = TRUE)
  }
  else if(num_remaining_elements_years < 10* 2.73972602739726)
  {
    returns <- try(ci(fit, return.period=c(2.73972602739726, 2*2.73972602739726, 5* 2.73972602739726, num_remaining_elements_years, 10 * 2.73972602739726, 20 * 2.73972602739726, 50*2.73972602739726)), silent = TRUE)
  }
  else if(num_remaining_elements_years < 20* 2.73972602739726)
  {
    returns <- try(ci(fit, return.period=c(2.73972602739726, 2*2.73972602739726, 5* 2.73972602739726, 10 * 2.73972602739726, num_remaining_elements_years, 20 * 2.73972602739726, 50*2.73972602739726)), silent = TRUE)
  }
  else if(num_remaining_elements_years < 50* 2.73972602739726)
  {
    returns <- try(ci(fit, return.period=c(2.73972602739726, 2*2.73972602739726, 5* 2.73972602739726, 10 * 2.73972602739726, 20 * 2.73972602739726, num_remaining_elements_years, 50*2.73972602739726)), silent = TRUE)
  }
  else
  {
    returns <- try(ci(fit, return.period=c(2.73972602739726, 2*2.73972602739726, 5* 2.73972602739726, 10 * 2.73972602739726, 20 * 2.73972602739726, 50*2.73972602739726, num_remaining_elements_years)), silent = TRUE)
  }
  print(returns)
  if(length(returns) < 18 || is.nan(returns[18]) || returns[1] < 0 || returns[2] < 0 || returns[3] < 0 
     || returns[4] < 0 || returns[5] < 0 
     || (returns[1] >  returns[2] && returns[2] > returns[3]) 
     || (returns[3] >  returns[4] && returns[4] >  returns[5])
     || (returns[13] > returns[14] && returns[14] > returns[15]) 
     || (returns[15] > returns[16] && returns[16] > returns[17])
  )
  {
    print("Not Valid---here1")
    print("--------------------------")
    next
  }
  else
  {
    print(returns)
    break
  }
}


