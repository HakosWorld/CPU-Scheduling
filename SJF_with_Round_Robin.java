import java.util.*;
/*---------------Project Team Members-------------------
     Name1: Haitham Abdullah Taher  202107520
     Name2: Ali Abbas Ali  202107809
     Name3: Yusuf Mahmood   202100550
     Name4: Hussain Ali Ahmed 202106117
     Name5: Abdullah Mohamed  202104275
 */
public class SJF_with_Round_Robin {
    private static final ArrayList<Process> readyQ = new ArrayList<Process>();
    private static final ArrayList<Process> readQCopy = new ArrayList<Process>();
    private static final  ArrayList<String> gattChart =  new ArrayList<String>();
    private static int currentTime = 0;
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Process newProcess = null;
        int quantum;
        String id;
        int aT, bT;


        while (true) {
            System.out.print("Enter process ID, arrival time, and burst time(0,0,0) to terminate: ");
            id = scan.next();
            aT = scan.nextInt();
            bT = scan.nextInt();

            if (id.equals("0") && aT == 0 && bT == 0)
                break;
            //Check if repeated id
            boolean isExist = false;
            for (Process p : readyQ) {
                if (id.equalsIgnoreCase(p.getPid())) {
                    System.out.println("Sorry the Id \"" + id + "\" is already exist, please try again");
                    isExist = true;
                    break;
                }
            }
            if (isExist)
                continue;

            newProcess = new Process(id, aT, bT);
            readyQ.add(newProcess);
        }
        //Sorting the ready queue based on the burst time to find the burst time that is greater than 80% of the process
        readyQ.sort(Comparator.comparing(Process::getBurstTime));
        quantum = calculateQuantum(readyQ.size());
        //Sorting the ready queue based on the arrival time, and in case processes have the same arrival:
        //the sorting will be according to the burst time of the process to reduce the waiting time.
        readyQ.sort(Comparator.comparing(Process::getArrivalTime).thenComparing(Process::getBurstTime));

        //Add the first time to Gantt Chart
        gattChart.add("0");

        //Building the Gantt chart using the shortest Job First and Round Robin algorithms
        for (int i = 0; i < readyQ.size(); i++) {
            Process p = readyQ.get(i);
            boolean isCurrentSufficient = false;
            //Testing for the idle state of the CPU
            while (currentTime < p.getArrivalTime()) {
                currentTime++;
                if (currentTime == p.getArrivalTime()) {
                    isCurrentSufficient = true;
                    gattChart.add("{idle" + "}" + currentTime);
                    i--;//since this iteration will not compete we will reprocess the same process in next iteration
                }
            }
            if (isCurrentSufficient)
                continue;
            //calculate the response time
            if (p.getRemainingBurst() == p.getBurstTime()) {
                p.setResponseTime(currentTime - p.getArrivalTime());
            }
            for (int j = 0; j < quantum && p.getRemainingBurst() > 0; j++) {

                p.setRemainingBurst(p.getRemainingBurst() - 1);
                currentTime++;
            }
            gattChart.add("{" + p.getPid() + "}" + currentTime);
            if (p.getRemainingBurst() == 0) {
                //Calculate the waiting time
                p.setWaitingTime(currentTime - p.getBurstTime() - p.getArrivalTime());
                //calculate the Turn Around Time
                p.setTurnAroundTime(currentTime - p.getArrivalTime());
                readQCopy.add(p);
                readyQ.remove(i);
                i--;// Because a process got removed
            } else {
                updateReady();
                i--;
            }

        }
        System.out.println("The Gantt Chart of the Shortest Job First with Round Robin with Quantum= " + quantum + ":");
        for (String p : gattChart)
            System.out.print(p);
        System.out.println("\n\nThe Execution Statistics: ");
        readQCopy.sort(Comparator.comparing(Process::getArrivalTime).thenComparing(Process::getBurstTime));
        double sumTurnaroundTime=0,sumResponseTime=0,sumWaitingTime=0;

        System.out.printf("%-15s %-15s %-15s %-15s%n", "Process ID", "Turnround Time", "Response Time","Waiting Time");
        System.out.printf("%s%n", "--------------------------------------------------------------");
        boolean hasNext = true;
        String lastProcessID = readQCopy.get(readQCopy.size()-1).getPid();
        while(hasNext){
            for(int i=0;i<readQCopy.size();i++){
                Process p = readQCopy.get(i);
                if(lastProcessID.equals(p.getPid()))
                    hasNext =false;
                sumTurnaroundTime += p.getTurnAroundTime();
                sumResponseTime +=p.getResponseTime();
                sumWaitingTime += p.getWaitingTime();
                System.out.printf("%-15s %-15d %-15d %-15d",p.getPid(),p.getTurnAroundTime(),p.getResponseTime(),p.getWaitingTime());
                System.out.println("\n--------------------------------------------------------------");
            }

        }
        int size = readQCopy.size();
        System.out.println("Average Statistics of all processes: ");
        System.out.println("Average Turnaround Time: "+(sumTurnaroundTime/size));
        System.out.println("--------------------------------------------------------------");
        System.out.println("Average Response Time: "+(sumResponseTime/size));
        System.out.println("--------------------------------------------------------------");
        System.out.println("Average Waiting Time: "+(sumWaitingTime/size));
        System.out.println("--------------------------------------------------------------");


    }

    // This method is for estimating the quantum value based on the size of the sorted ready queue
    private static int calculateQuantum(int listSize){
        // estimating the index of the quantum value that covers 80% of the processes and to making sure
        // the index estimated is not equal to the list size by taking the minimum between size and size-1
        int index = Math.min((int)(Math.ceil(listSize*0.8)),listSize-1);
        //This will be helpful in case there is less than 10 processes
        if(index==listSize-1)
            index--;

        return readyQ.get(index).getBurstTime();

    }

    //This method will rearrange the ready queue after each quantum period
    private static void updateReady(){
        Process Process_toBe_Shifted = readyQ.get(0);
        if(readyQ.size()==1)
            return;
        // The following code is to shift the current process to the
        // right place in the ready queue after quantum period finishes
        int i;
        for(i=0;i<readyQ.size()-1;i++) {
            if (readyQ.get(i+1).getArrivalTime() > currentTime) {
                break;
            }
            readyQ.set(i, readyQ.get(i + 1));
        }
        readyQ.set(i,Process_toBe_Shifted);
    }

}