import model.Push;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

public class LogAnalyzer {

    static class Pair {
        private long id;
        private long amount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair pair = (Pair) o;
            return getId() == pair.getId();
        }

        @Override
        public int hashCode() {

            return Objects.hash(getId());
        }

        public long getId() {
            return id;
        }

        public Pair setId(long id) {
            this.id = id;
            return this;
        }

        public long getAmount() {
            return amount;
        }

        public Pair setAmount(long amount) {
            this.amount = amount;
            return this;
        }
    }

    public static void scanByMap(String line, List<Pair> pushes, Map<Long, Long> mapper) {
        if (line.contains("Receiving and sending push with id:")
                ||
                line.contains("push was sended with id:")) {
            Pair push = new Pair().setId(Integer.parseInt(line.substring(line.lastIndexOf(":") + 1, line.lastIndexOf(";"))));
            if(pushes.contains(push)){
                push=pushes.get(pushes.indexOf(push));
                push.setAmount(push.getAmount()+1);
            }else{
                pushes.add(push.setAmount(1));
            }
            mapper.put(Long.parseLong(line.substring(line.lastIndexOf(":") + 1, line.lastIndexOf(";"))),push.getAmount());
        }

    }


    public static void main(String[] args) {
        HashMap<Long,Long> mapper = new HashMap<>();
        ArrayList<Pair> pushes = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("mylogs.log"))) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                scanByMap(line,pushes, mapper);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mapper.forEach((k,v)->{
            if(v>2) {
                System.out.println(k + " " + v);
            }
        });
    }

}
