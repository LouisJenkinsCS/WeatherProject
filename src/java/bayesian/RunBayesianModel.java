/*
 * Fitting the Bayesian model using JAGS
 * In the package, APIs of JAGS have been defined
 * 
 * Since Javascript will be used to draw the plots
 * instead of using Java graphics, the program will
 * generate the JS file to draw the plots.
 */
package bayesian;
import async.Data;
import async.DataReceiver;
import async.DataValue;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.*;
import java.time.Instant;
import java.time.Period;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Dong Zhang
 */
public class RunBayesianModel {
    /* 
    * Some arguments will be passed to here using args: args[1] is model text file. 
    * Or you can use a confid file but involving File IO Stream here.
    */
    private static String model_name = "BASE_metab_model_v2.1.txt";
    private static String user_dir = RunBayesianModel.class.getResource(".").getPath().substring(1);
    private static String base_dir = user_dir + "BASE\\";
    private static String input_dir = base_dir + "input\\"; 
    private static String output_dir = base_dir + "output\\";
    private static String jsroot_dir = base_dir + "JSchart\\";
    private static int interval = 600; // IMO, this variable is used to generate instant_rate table.
    private static final long PAR = 637957793;
    private static final long HDO = 1050296639;
    private static final long Temp = 639121399;
    private static final long Pressure = 639121405;
    private static final double ATMOSPHERIC_CONVERSION_FACTOR = 0.000986923;
    
    private static String EXAMPLE = "";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        
        FileOperation fo = new FileOperation();
        
        Data data = DataReceiver.getData(Instant.now().minus(Period.ofWeeks(4)), Instant.now().minus(Period.ofWeeks(4)).plus(Period.ofDays(1)), PAR, HDO, Temp, Pressure);
        ForkJoinPool.commonPool().execute(() -> runJJAGSForCSV(fo, data));
        
        EXAMPLE = DataReceiver.getData(Instant.now().minus(Period.ofWeeks(4)), Instant.now().minus(Period.ofWeeks(4)).plus(Period.ofDays(1)), PAR, HDO, Temp, Pressure)
                .getData()
                .groupBy(dv -> dv.getId())
                .flatMap(group -> {
                    String header;
                    if (group.getKey() == PAR) {
                        return group.sorted()
                                .map(dv -> dv.getValue())
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("PAR Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("PAR <- c("::concat);
                    } else if (group.getKey() == HDO) {
                        return group.sorted()
                                .map(dv -> dv.getValue())
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("HDO Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("DO.meas <- c("::concat);
                    } else if (group.getKey() == Temp) {
                        return group.sorted()
                                .map(dv -> dv.getValue())
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("Temp Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("tempC <- c("::concat);
                    } else if (group.getKey() == Pressure) {
                        return group.sorted()
                                .map(dv -> dv.getValue() * ATMOSPHERIC_CONVERSION_FACTOR)
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("Pressure Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("atmo.pressure <- c("::concat);
                    } else {
                        throw new RuntimeException("Bad Key: " + group.getKey());
                    }
                })
                .map(str -> str + ")")
                .reduce("", (str1, str2) -> str1 + str2 + "\n")
                .map(str -> str + "\nsalinity <- c(" + IntStream.range(0, 96).map(x -> 0).mapToObj(Integer::toString).collect(Collectors.joining(",")) + ")")
                .blockingGet();
        
        // For each CSV file
//        for (File f : fo.fileFilter(input_dir, "csv")) {
//        }
        
        ForkJoinPool.commonPool().awaitQuiescence(Integer.MAX_VALUE, TimeUnit.DAYS);
    }
    
    
    private static void runJJAGSForCSV(FileOperation fo, Data data) {
        String fname = "tmp.csv";
        //Fitting the model and obtain the results in the folder output/
        String tmp_dir = base_dir + fname + "/";

        // Create the temporary folder for current CSV calculation
        fo.newFolder(tmp_dir);
        // Create the output folder for current CSV calculation inside output/
        fo.newFolder(output_dir + fname);

        // Prepare the data
        csvReader content = new csvReader();
//        String cnt = content.read(input_dir + f.getName(), 3);
        String cnt = data.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .groupBy(dv -> dv.getId())
                .flatMap(group -> {
                    String header;
                    if (group.getKey() == PAR) {
                        return group.sorted()
                                .map(dv -> dv.getValue())
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("PAR Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("PAR <- c("::concat);
                    } else if (group.getKey() == HDO) {
                        return group.sorted()
                                .map(dv -> dv.getValue())
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("HDO Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("DO.meas <- c("::concat);
                    } else if (group.getKey() == Temp) {
                        return group.sorted()
                                .map(dv -> dv.getValue())
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("Temp Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("tempC <- c("::concat);
                    } else if (group.getKey() == Pressure) {
                        return group.sorted()
                                .map(dv -> dv.getValue() * ATMOSPHERIC_CONVERSION_FACTOR)
                                .buffer(Integer.MAX_VALUE)
                                .doOnNext(list -> System.out.println("Pressure Count: " + list.size()))
                                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                                .map("atmo.pressure <- c("::concat);
                    } else {
                        throw new RuntimeException("Bad Key: " + group.getKey());
                    }
                })
                .map(str -> str + ")")
                .reduce("", (str1, str2) -> str1 + str2 + "\n")
                .map(str -> str + "\nsalinity <- c(" + IntStream.range(0, 96).map(x -> 0).mapToObj(Integer::toString).collect(Collectors.joining(",")) + ")")
                .blockingGet();;
        
        content.nRow = 92;
        System.out.println("Row: " + content.nRow);
        System.out.println(cnt);
        // String cnt = content.read(input_dir + f.getName(), 3, 4);
        String datafile = "num.measurements <- c(" + content.nRow + ")\n"
                + "interval <- c(" + interval + ")\n"
                + cnt;
        fo.newFile(tmp_dir + "data.txt", datafile);

        // JAGS object
        // If you have path to jags in your PATH, you can use "jags" only without path to it.
        // ---- Windows JAGS ----
         JJAGS jg = new JJAGS("\"C:/Program Files/JAGS/JAGS-4.2.0/x64/bin/jags.bat\"", tmp_dir, fname, base_dir + model_name);
        // ---- Linux JAGS ----
//            JJAGS jg = new JJAGS("/usr/bin/jags", tmp_dir, base_dir + model_name);            

        // Variables to be monitored. They will be used to create graphs. You can read a config file to get them.
        String[] monitor = {"A","R","K","K.day","p","theta","sd","ER","GPP", "NEP","sum.obs.resid","sum.ppa.resid","PPfit","DO.modelled"};  
        String[] init = {""};   // Initial values to the model, can be blank.
        int nchains = 3, nthin = 10, niter = 20000, nburnin = (int) (niter * 0.5);
        jg.make_script(nchains, nthin, monitor, nburnin, niter);
        jg.make_init(123, init); // Random seed can be changed for different CSV files
        jg.jags_run();  // Here if the app running properly, you will get "Job done" in the terminal
        

        // Clean the cache of calculation
        fo.delFolder(tmp_dir);

        /*
        Here, the output TXT files will be converted to JSON format.
        Result files in output_dir/f.getName()/ are:
        1. CODAIndex.txt --  Telling the row index of core results
        2. CODAchain?.txt -- A sequence of core results for each chain
        * Well, it seems convert TXT to JSON is useless...  (=,=)#
        * Since JS is limited to access local files, the better way is
        * write all values to JS files directly and loaded in html, which
        * is written in the next section.
        */
        //resultLoader rl = new resultLoader(output_dir + f.getName());
        //rl.JSONconvertIndex();
        //for(int i=1; i<= nchains; i++){
        //    rl.JSONconvertChain(i);
        //}

        /*
         * It will be the Javascript JS file's job to create charts.
         * Methods to recognize all needed variables must be written,
         * and covert it to JSON format and save to JS files.
         * The Template.html will be used to create html files for each CSV
         * Note: The template is NOT a completed html file, therefore
         * JS codes can be appended to them. The porgram will complete them
         * after pushing codes, thus they can be run directly in explorer.
        */
        // Create html file and JS files for the CSV file using the template
        fo.copyFile(jsroot_dir + "Chart.bundle.js", output_dir + fname + "/" + "Chart.bundle.js");
        fo.copyFile(jsroot_dir + "jquery-3.1.1.min.js", output_dir + fname + "/" + "jquery-3.1.1.min.js");
        fo.newFolder(output_dir + fname + "/jsdata");

        resultLoader rl = new resultLoader(output_dir + fname);
        // Parsing all variables out from TXT outputs and create JS files for each
        rl.PrepareDataJS(content.nRow, nchains, niter, nthin, fname);
        // Generate the graph JS file
        String plotVarList = "A,R,p,K.day,theta"; // Do NOT place "spaces"!!!
        String scatterList = "PAR,tempC,DO.modelled";   // SAME as above!!!
        JSgen csvjs = new JSgen(output_dir + fname);
        csvjs.CodingHTML(fname, (plotVarList + "," + scatterList).split(","), content.nRow);
        csvjs.codingJS(nchains, plotVarList.split(","), scatterList.split(","));
        csvjs.codingStatJS(content.nRow, (int)(niter/nthin), nchains);

        // Scatter plots of PAR and tempC
        data
                .getData()
                .subscribeOn(Schedulers.computation())
                .filter(dv -> dv.getId() == PAR)
                .sorted()
                .map(DataValue::getValue)
                .map(Object::toString)
                .buffer(Integer.MAX_VALUE)
                .reduce("", (str1, str2) -> str1 + str2 + "\n")
                .subscribe(val -> rl.Str2JS(val, fname, "PAR"));
//        int offset = 6;
//        String tmp = content.read(input_dir + fname, 3, 3);
//        String currentVarName = "PAR";
//        tmp = tmp.substring(offset + currentVarName.length(), tmp.length()-2);
//        System.out.println(tmp);
//        rl.Str2JS(tmp, fname, currentVarName);
        
        data
                .getData()
                .subscribeOn(Schedulers.computation())
                .filter(dv -> dv.getId() == Temp)
                .sorted()
                .map(DataValue::getValue)
                .map(Object::toString)
                .buffer(Integer.MAX_VALUE)
                .reduce("", (str1, str2) -> str1 + str2 + "\n")
                .subscribe(val -> rl.Str2JS(val, fname, "tempC"));
//        tmp = content.read(input_dir + fname, 4, 4);
//        currentVarName = "tempC";
//        tmp = tmp.substring(offset + currentVarName.length(), tmp.length()-2);
//        rl.Str2JS(tmp, fname, "tempC");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(RunBayesianModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
