import javax.swing.*;
import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;


interface Action {

    public boolean get();  //returns basket or cubicle (when someones depature from cubicle)

    public void getWaitingTime(Person who); //randomly gets sleepingTime

    public void waiting(int sleepTime); //takes time for waiting

}

class Basket extends Pool {
    public synchronized boolean get(Person person) {
        return super.get();
    }
}

class Cubicle extends Pool {

    public synchronized boolean get(Person person) {
        return super.get();
    }
}

class Pool implements Action {
    private static int basket; //a pool that possesses the number of baskets
    private static int cubicle;//a pool that possesses the number of cubicles
    public static boolean available = true; //judgues if resoures can be distributed now
    public static int depature = 0; //statistics depatures
    static viewer viewer = null; //allocate a viewer to pool

    /*
     * List is responsible for managing resouces (basket and cubicle)
     */

    static List<Pool> BasketList = new ArrayList<>();
    static List<Pool> CubicleList = new ArrayList<>();

    //default the number of baskets and cublicles
    Pool() {
        this.basket = 5;
        this.cubicle = 3;
    }

    Pool(int basket, int cubicle) {
        this.basket = basket;
        this.cubicle = cubicle;
        setManager();
    }

    //put baskets and cubicles into individual list
    public void setManager() {
        for (int n = 0; n < this.basket; n++) {
            Pool newBasket = new Basket();
            BasketList.add(newBasket);
        }

        for (int n = 0; n < this.cubicle; n++) {
            Pool newCubicle = new Cubicle();
            CubicleList.add(newCubicle);
        }

    }

    public int getBasket() {
        return BasketList.size();
    }

    public int getCubicle() {
        return CubicleList.size();
    }

    static synchronized void CubicleAdd(Pool cubicle) {
        CubicleList.add(cubicle);
    }

    static synchronized void BasketAdd(Pool basket) {
        BasketList.add(basket);
    }


    public boolean get() {
        //when a person is trying to depart , don't forget to return basket
        if (this.getClass().equals(Basket.class))
            BasketAdd(this);

        //when a person is trying to depart , don't forget to return cubicle
        if (this.getClass().equals(Cubicle.class))
            CubicleAdd(this);

        return true;
    }

    /*
     * "acutural_distrubtion" will acturally distribute resoures in real
     */
    static synchronized void actural_distribution(Person person) {
        if (person.getPerson_Basket() == 0) {
            person.newBasket = BasketList.get(0);
            BasketList.remove(0);
            person.basket++;
        }
        person.newCubicle = CubicleList.get(0);
        CubicleList.remove(0);
        available = false;
    }

    /*
     * "distrubtion" is to determine if available resources left
     */
    public synchronized boolean distribution(Person person) {
        //if available is false then person will be forced to wait
        while (!available) {
            {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        //ensures basket and cubicle both are getting enough
        if ((BasketList.size() + person.getPerson_Basket() > 0 && CubicleList.size() > 0)) {
            viewer.state_loading(this, person);
            notify();
        } else
            return false;

        return true;
    }

    public void getWaitingTime(Person person) {
        //randomly sleeping ( time for dressing )
        person.sleepingTime = (int) (Math.random() * 10 + 1);
        actural_distribution(person);
        if (person.getPriority() == person.MAX_PRIORITY) {
            System.out.println("a person : " + person.getName()
                    + " has gotten a basket and a cubicle " + "and will occupy cubicle at "
                    + person.sleepingTime + "seconds" + "(e)(f)");
        } else

        {
            System.out.println("a person : " + person.getName()
                    + " has gotten a basket and a cubicle " + "and will occupy cubicle at "
                    + person.sleepingTime + "seconds" + "(a)(b)(c)");
        }

    }

    public void waiting(int sleepTime) {
        try {
            Thread.currentThread().sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static String state() {
        return ((""
                + "basket : " + BasketList.size() + "\n"
                + "cubicle : " + CubicleList.size())
                + "\n------------------------------\n");
    }
}

class Person extends Thread {
    Pool newPool = null;
    Pool newBasket = null;
    Pool newCubicle = null;
    public int sleepingTime = 0; //waiting time
    public int basket = 0;
    static viewer viewer = null;
    static update updater = null;

    Person(Pool pool) {
        this.newPool = pool;
        this.viewer = pool.viewer; //allocate a viewer
        this.updater = viewer.updater; //allocate a updater
    }

    public int getPerson_Basket() {
        return this.basket;
    }


    public void run() {

        /*
         * startring threads after 0.3 to 2 sec
         */
        try {
            this.sleep((long) (Math.random() * 2000 + 300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
         * (a)(b)(c)
         */
        while (!newPool.distribution(this)) ; //distributions starting (distributes resources if they are all available)
        newPool.waiting(sleepingTime);//starts dressing according to sleepTime

        /*
         * update data
         */
        try {
            updater.setUpdate(newCubicle, this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
         * (d)
         */
        System.out.println("a person : " + Thread.currentThread().getName()
                + " is swiming" + "(d)");

        this.setPriority(MAX_PRIORITY);//depature is given a high priority

        /*
         * (e)(f) returns basket and cubicle
         */
        while (!newPool.distribution(this)) ;//distributions starting (distributes resources if they are all available)
        newPool.waiting(sleepingTime);//starts dressing according to sleepTime

        /*
         * update datas
         */
        try {
            //returns cubicle first nad then returns basket
            updater.setUpdate(newCubicle, this);
            updater.setUpdate(newBasket, this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("a person : " + Thread.currentThread().getName()
                + " has departed from pool" + "(complete)");

        newPool.depature++; //statistics depatures amount

        /*
         * if all people has depatured , stop viewer
         */
        if (viewer.population == newPool.depature) {
            System.out.println("\n------------------------------\ndone");
            super.stop();//stop viewer
        }
        this.stop();//stops thread
    }
}

class viewer extends Thread {
    Pool who = null; // who can be pool/basket/cubicle
    Person person = null;
    static int population; //people amounts
    static List<Thread> list = null; //all person is in here
    static update updater = new update(); //al

    viewer(Pool p, List<Thread> list) {
        this.list = list;
        this.who = p;
        p.viewer = this;
    }

    viewer() {
    }

    /*
     * update datas according to who comes in ("who" is pool or basket or cubicle)
     */
    synchronized static void state_incidence(Pool who, Person person) {

        if (who.getClass().equals(Pool.class)) {
            who.getWaitingTime(person);
        } else if (who.getClass().equals(Basket.class)) {
            person.basket--;
            who.get();
            System.out.println(person.getName() + " returns basket" + "\n");
        } else if (who.getClass().equals(Cubicle.class)) {
            who.get();

            System.out.println(person.getName() + " returns cubicle" + "\n");
        }
        System.out.println(who.state());

    }

    /*
     * when distribution available , calls this function to update datas
     */
    synchronized void state_loading(Pool who, Person person) {

        try {
            updater.setUpdate(who, person);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        who.available = true;
        notifyAll();
    }

    public void run() {

        //here is population setting
        for (int n = 0; n < 10; n++) {
            Thread thread = new Person(who);//here (who) is Pool
            list.add(thread);
        }

        //starting people(thread)
        for (Thread l : list) {
            l.start();
        }
        population = list.size();
        System.out.println(who.state());
        //state_loading(who, person);
    }

}

class update extends Thread {
    /*
     * ensures datas' updating accuracy , we need to know who wants to udate datas
     */
    static Pool pool;
    static Person person;
    static viewer viewer;
    Thread updater = null;

    synchronized void setUpdate(Pool pool, Person person) throws InterruptedException {

        /*
         * points to data's possessors
         */

        this.pool = pool;
        this.person = person;
        this.viewer = pool.viewer;

        if (updater == null)
            updater = new update();

        updater.run();
        updater.join();//other instructions will temporarily suspended until "updater.run()" finished
    }

    public void run() {
        viewer.state_incidence(pool, person);
    }
}



public class gui extends JFrame{
    private PrintStream standardOut;
    public static int go = 0;
    private JButton buttonStart = new JButton("Start");
    public JTextArea textArea;
    private JButton buttonClear = new JButton("Clear");
    private JPanel panelMain = new JPanel();
    public gui(){
        super("Os");

        textArea = new JTextArea(50, 10);
        textArea.setEditable(false);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));

        // keeps reference of standard output stream
        standardOut = System.out;

        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);

        // creates the GUI
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.anchor = GridBagConstraints.WEST;

        add(buttonStart, constraints);

        constraints.gridx = 1;
        add(buttonClear, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        add(new JScrollPane(textArea), constraints);

        // adds event handler for button Start
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                go = 1;

            }
        });

        // adds event handler for button Clear
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // clears the text area
                try {
                    textArea.getDocument().remove(0,
                            textArea.getDocument().getLength());
                    standardOut.println("Text area cleared");
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);    // centers on screen
    }




//        buttonStart.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                go = 1;
//            }
//        });


    public static void main(String[] args)throws InterruptedException{

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new gui().setVisible(true);
            }
        });
//
//        JFrame frame = new JFrame("swimPool");
//        frame.setContentPane(new gui().panelMain);
//        frame.add(new gui().startButton);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);

        while(go == 0){
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
            }
        }

        Pool pool = new Pool();//creadte new pool

        List<Thread> list = new ArrayList<>();//put all peopole (thread) into list
        pool.setManager();//manage threads' baskets and cubicles states

        Thread viewer = new viewer(pool, list);//additional thread for checking other threads
        viewer.start();//notice that it can be used for updating states and managing people

    }
}
