import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class EntryPoint {
    public static void main(String[] args) throws IOException, SQLException {
        final String backupId = (args != null && args.length > 0) ? args[0] : "f75b2e6643e698e2933c16a0c880780d6412ec4e";
        final int chatId = Integer.parseInt((args != null && args.length > 1) ? args[1] : "3");

        //the hash based filename is always the same for the iMessages backup SQLite DB
        final String file = String.format("/Users/lthompson/Library/Application Support/MobileSync/Backup/%s/3d/3d0d7e5fb2ce288813306e4d4636395e047a3d28", backupId);
        final String url = String.format("jdbc:sqlite:%s", file);

        //read the sql lite DB to a file
        try(
                Connection conn = DriverManager.getConnection(url);
                FileWriter fileWriter = new FileWriter("./messages.txt")
        ){
            String sqlStatement = Files.readString(Path.of("./query.sql"));
            PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement);
            preparedStatement.setInt(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                int fromMe = resultSet.getInt(2);
                String text = resultSet.getString(3);
                String row = String.format("%s: %s\n", fromMe == 1 ? "To  " : "From", text);
                fileWriter.write(row);
            }
        }

        //filter out some stuff from the file to create a word cloud from without some of the junk
        try(
                FileInputStream fileInputStream = new FileInputStream("./messages.txt");
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                FileWriter fileWriter = new FileWriter("./messages-words.txt")
        ){
            String line;
            while((line = bufferedReader.readLine()) != null){

                if (line.startsWith("To  : ") || line.startsWith("From: ")) {
                    line = line.substring(5);
                }

                line = line.trim();
                if (!line.isBlank()){
                    if (!line.startsWith("http")){
                        if (!line.equalsIgnoreCase("￼")) {
                            if (!line.startsWith("Liked “")) {
                                if (!line.startsWith("Loved “")) {
                                    System.out.println(line);
                                    fileWriter.write(line);
                                    fileWriter.write('\n');
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
