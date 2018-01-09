import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface Action {

    public boolean get();  //returns basket or cubicle (when depaturing from dressing room)
    public void getWaitingTime(Person who); //randomly sleeping ( time for dressing )
    public void waiting(int sleepTime);

}

class Basket extends Pool
{
    public synchronized boolean get(Person person) {return super.get();}
}

class Cubicle extends Pool
{

    public synchronized boolean get(Person person) {return super.get();}
}

class Pool  implements Action {
    private static int basket;
    private static int cubicle;
    public static boolean available=true;
    public static int depature=0; //statistics depatures
    static viewer viewer; //allocate a view to pool
    /*
     * List is responsible for managing List (to judge if Basket/Cubicle are available or not?)
     */
    static List<Pool> BasketList = new ArrayList<>();
    static List<Pool> CubicleList = new ArrayList<>();

    //default the number of baskets and cublicles
    Pool(){
        this.basket = 1200;
        this.cubicle = 1000;
    }

    Pool (int basket , int cubicle)
    {
        this.basket = basket;
        this.cubicle = cubicle;
        setManager();
    }
    public int getBasket(){return BasketList.size();}
    public int getCubicle(){return CubicleList.size();}
    //put baskets and cubicles into manager
    public void setManager()
    {
        for(int n=0;n<this.basket;n++)
        {
            Pool newBasket = new Basket();
            BasketList.add(newBasket);

        }

        for(int n=0;n<this.cubicle;n++)
        {
            Pool newCubicle = new Cubicle();
            CubicleList.add(newCubicle);
        }

    }
    static synchronized void CubicleAdd(Pool cubicle){CubicleList.add(cubicle);}
    static synchronized void BasketAdd(Pool basket){BasketList.add(basket);}
    public  boolean get()
    {
        //when a person is trying to depart , don't forget to return basket
        if(this.getClass().equals(Basket.class))
            BasketAdd(this);

        //when a person is trying to depart , don't forget to return cubicle
        if(this.getClass().equals(Cubicle.class))
            CubicleAdd(this);

        return true;
    }
    static synchronized void actural_distribution(Person person)
    {
        if(person.getPerson_Basket()==0)
        {
            person.newBasket= BasketList.get(0);
            BasketList.remove(0);
        }
        person.newCubicle= CubicleList.get(0);
        CubicleList.remove(0);
        available=false;
    }
    public  synchronized boolean distribution(Person person)
    {
        //ensure basket and cubicle is getting enough

        while(!available)
        {
            {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if((BasketList.size() + person.getPerson_Basket() >0 && CubicleList.size()>0))
        {
            viewer.state_loading(this, person);
            notify();
        }
        else
            return false;

        return true;
    }
    public void getWaitingTime(Person person) {
        //randomly sleeping ( time for dressing )
        person.sleepTime = (int) (Math.random()*10+1);
        actural_distribution(person);
        if(person.getPriority()==person.MAX_PRIORITY)
        {
            System.out.println("a person : " + person.getName()
                    + " has gotten a basket and a cubicle " + "and will occupy cubicle at "
                    + person.sleepTime + "seconds"+"(e)(f)");
        }
        else

        {
            System.out.println("a person : " + person.getName()
                    + " has gotten a basket and a cubicle " + "and will occupy cubicle at "
                    + person.sleepTime + "seconds"+"(a)(b)(c)");
        }

    }
    public void waiting(int sleepTime)
    {
        try {
            Thread.currentThread().sleep(sleepTime*1000);
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    static String state(){
        return((""
                +"basket : " + BasketList.size()+"\n"
                +"cubicle : " + CubicleList.size())
                +"\n------------------------------\n");
    }
}
class Person extends Thread {
    Pool newPool =null;
    Pool newBasket =null;
    Pool newCubicle =null;
    public int sleepTime=0;
    public int basket=0;
    static viewer viewer=null;
    static update updater;

    Person(Pool pool)
    {
        this.newPool = pool;
        this.viewer=pool.viewer;
        this.updater=viewer.updater;
    }
    public int getPerson_Basket() {return this.basket;}


    public void run() {

        try{
            this.sleep((long) (Math.random()*2000+300));
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        //(a)(b)(c) returns cubicle
        while(!newPool.distribution(this));

        // newPool.getWaitingTime(this);

        newPool.waiting(sleepTime);//starts dressing according to sleepTime

        try {
            updater.setUpdate(newCubicle,this);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //  viewer.state_incidence(newCubicle,this);//departure from cubicle
        basket++; //arrival is given a new basket

        //(d)
        System.out.println("a person : " + Thread.currentThread().getName()
                + " is swiming" + "(d)" );
        //depature is given a high priority
        this.setPriority(MAX_PRIORITY);

        //(e)(f) returns basket and cubicle
        while(!newPool.distribution(this));
        //starts dressing according to sleepTime

        newPool.waiting(sleepTime);//starts dressing according to sleepTime
        try {
            updater.setUpdate(newCubicle,this);
            updater.setUpdate(newBasket,this);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //viewer.state_incidence(newCubicle,this); // returns cubicle and update datas
        //viewer.state_incidence(newBasket,this); // returns basket and update datas

        System.out.println("a person : " + Thread.currentThread().getName()
                + " has departed from pool" + "(complete)");
        newPool.depature++;
        if(viewer.population==newPool.depature)
        {
            System.out.println("has done");
            super.stop();
        }
        this.stop();//stops thread
    }
}
class viewer extends Thread {
    Pool who=null;
    Person person;
    static int population; //people amounts
    static List<Thread> list; //all person is in here
    static update updater = new update();
    viewer(Pool p , List<Thread> list)
    {
        this.list=list;
        this.who=p;
        p.viewer=this;
    }
    viewer()
    {
    }
    synchronized static void state_incidence(Pool who , Person person) {

        if(who.getClass().equals(Pool.class))
        {
            who.getWaitingTime(person);
        }
        //if the one is person then get watingTime(maybe do something like dressing or swimming)
        else if(who.getClass().equals(Basket.class))
        {
            person.basket--;
            who.get();
            System.out.println(person.getName() + " returns basket"+"\n");
        }

        else if(who.getClass().equals(Cubicle.class))
        {
            who.get();

            System.out.println(person.getName() + " returns cubicle"+"\n");
        }
        System.out.println(who.state());

    }
    //when distribution finished , calls this function to update datas
    synchronized void state_loading(Pool who , Person person) {

        try {
            updater.setUpdate(who, person);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        who.available=true;

        notifyAll();
    }
    public void run()
    {

        //here is population setting
        for(int n=0;n<2000;n++)
        {
            Thread thread = new Person(who);//here (who) is Pool
            list.add(thread);
        }

        //starting people(thread)
        for(Thread l :list)
        {
            l.start();
        }
        population=list.size();
        System.out.println(who.state());
        //state_loading(who, person);
    }

}
class update extends Thread
{
    static Pool pool;
    static Person person;
    static viewer viewer;
    synchronized void setUpdate(Pool pool,Person person) throws InterruptedException{
        this.pool=pool;
        this.person=person;
        this.viewer=pool.viewer;
        Thread updater=new update();


        updater.start();
        updater.join();
    }
    public void run()
    {

        viewer.state_incidence(pool,person);
        stop();
    }
}
public class SP {

    public static void main(String[] args) throws InterruptedException
    {

        Pool pool=new Pool();//creadte new pool

        List<Thread> list =new ArrayList<>();//put all peopole (thread) into list
        pool.setManager();//manage threads' baskets and cubicles states

        Thread viewer= new viewer(pool,list);//additional thread for checking other threads

        viewer.start();//notice that it can be used for updating states and managing people
    }
}
