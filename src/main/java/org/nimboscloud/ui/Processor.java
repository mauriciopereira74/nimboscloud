package org.nimboscloud.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {

    private Map<String, Pattern> operationPatterns = new HashMap<>();
    private Map<String, Method> operations = new HashMap<>();

    private int range = 0;
    private String loggedInAs = "";

    public Processor() throws NoSuchMethodException {

        operations.put("help", this.getClass().getMethod("processHelp", String.class));

        operationPatterns.put("login", Pattern.compile("login\\s+(\\w+)\\s+(\\w+)"));
        operations.put("login", this.getClass().getMethod("processLogin", String.class));

        operationPatterns.put("register", Pattern.compile("register\\s+(\\w+)\\s+(\\w+)"));
        operations.put("register", this.getClass().getMethod("processRegister", String.class));

        operationPatterns.put("logout", Pattern.compile("logout\\s+(\\w+)"));
        operations.put("logout", this.getClass().getMethod("processLogout", String.class));

    }


/*    public void processLogout(String userCommand) throws IOException, InterruptedException {

        Pattern parkPattern = operationPatterns.get("logout");
        Matcher m = parkPattern.matcher(userCommand);

        if (m.find()) {

            String username = m.group(1);

            if (Objects.equals(username, loggedInAs)) {

                boolean statusLogout = authManager.logoutUser(username);
                if (!statusLogout) System.out.println("nimbouscloud.error> You cannot logout if you are not logged in.");
                else System.out.println("nimbouscloud.info> Successfully logged out!");

                loggedInAs = "";

            } else
                System.out.println("nimbouscloud.error> You cannot sign out user '" + username + "' when you are using another account.");

        } else System.out.println("nimbouscloud.error> Invalid usage of 'logout' command, check the help menu.");
    }*/


    public void processHelp(String userCommand) {

        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("nimbouscloud.help> 'help' displays this message\n");

        helpMenu.append("nimbouscloud.help> 'register [username] [password]' register a new user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'login [username] [password]' login the user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'logout [username]' logout the user provied in the username");
        helpMenu.append("nimbouscloud.help> 'status' displays information about the user\n");
        helpMenu.append("nimbouscloud.help> 'notif [on/off]' enables or disables push notifications\n");
        helpMenu.append("nimbouscloud.help> 'notiflocation [x] [y] [radius]' add new location and radius to receive notifications\n");

        helpMenu.append("nimbouscloud.help> 'setlocation [x] [y]' set the user location to (x,y) coordinates\n");
        helpMenu.append("nimbouscloud.help> 'setrange [range]' set range to search scooters for\n");

        helpMenu.append("nimbouscloud.help> 'rent' make a reservation for a scooter within the provided range\n");
        helpMenu.append("nimbouscloud.help> 'park [reservation_code] [x] [y]' park the scooter indicated by the provided reservation code at (x,y) coordinates.\n");
        helpMenu.append("nimbouscloud.help> 'list' list available scooters within the range\n");
        helpMenu.append("nimbouscloud.help> 'listr' list available rewards within the range");

        System.out.println(helpMenu);
    }
/*    public void processRegister(String userCommand) throws IOException, InterruptedException {

        Pattern registerPattern = operationPatterns.get("register");
        Matcher m = registerPattern.matcher(userCommand);

        if (m.find()) {

            String username = m.group(1);
            String password = m.group(2);

            boolean registerStatus = authManager.registerUser(username, password);

            if (registerStatus) System.out.println("nimbouscloud.info> Successfully registered as '" + username + "'.");
            else System.out.println("nimbouscloud.error> Account already exists!");
        } else System.out.println("nimbouscloud.error> Invalid usage of 'register' command, check the help menu.");
    }*/

/*    public void processLogin(String userCommand) throws IOException, InterruptedException {

        Pattern loginPattern = operationPatterns.get("login");
        Matcher m = loginPattern.matcher(userCommand);

        if (m.find()) {

            String username = m.group(1);
            String password = m.group(2);

            boolean loginStatus = authManager.loginUser(username, password);

            if (loginStatus) {
                System.out.println("nimbouscloud.info> Successfully logged in as '" + username + "'.");
                loggedInAs = username;
            } else System.out.println("nimbouscloud.error> Invalid username or password.");
        } else System.out.println("nimbouscloud.error> Invalid usage of 'login' command, check the help menu.");
    }*/
    public void process(String userCommand) throws InvocationTargetException, IllegalAccessException {

        String possibleCommand = userCommand.strip();
        if (userCommand.contains(" ")) possibleCommand = userCommand.substring(0, userCommand.indexOf(' '));

        if (!operations.containsKey(possibleCommand)) {
            System.out.println("Invalid syntax, use the command 'help' to check every operation.");
            return;
        }

        Method processor = operations.get(possibleCommand);
        processor.invoke(this, userCommand);
    }
}
