import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface Action
{
    //returns basket or cubicle (when depaturing from dressing room)
    public boolean get();
    //randomly sleeping ( time for dressing )
    public void waiting_dressing();
}

class Basket extends Pool
{
    public synchronized boolean get() {return super.get();}
}

class Cubicle extends Pool
{
    public synchronized boolean get() {return super.get();}
}

class Pool implements Action{
    private static int basket;
    private static int cubicle;
    public boolean available=false;
    /*
     * Manger is responsible for managing List (to judge if Basket/Cubicle are available or not?)
     * true == available , false == is occupied
     */
    static List<Pool> BasketList = new ArrayList<>();
    static List<Pool> CubicleList = new ArrayList<>();
    static Map<Boolean, List<Pool>> BasketManager = new HashMap<>();
    static Map<Boolean, List<Pool>> CubicleManager = new HashMap<>();
    //default the number of baskets and cublicles
    Pool(){
        this.basket = 20;
        this.cubicle = 10;
    }

    Pool (int basket , int cubicle , String name)
    {
        this.basket = basket;
        this.cubicle = cubicle;
        setManager();
    }
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
        BasketManager.put(true, BasketList);
        CubicleManager.put(true, CubicleList);
    }

    public synchronized boolean get()
    {
        //when a person is trying to depart , don't forget to return basket
        if(this.getClass().equals(Basket.class))
        {
            BasketList.add(this);
            System.out.println(Thread.currentThread().getName() + " returns basket");
        }

        //when a person is trying to depart , don't forget to return cubicle
        if(this.getClass().equals(Cubicle.class))
        {
            CubicleList.add(this);
            waiting_dressing();
            System.out.println(Thread.currentThread().getName() + " returns cubicle");
        }

        return true;
    }
    public synchronized boolean distribution(Person thread)
    {
        //ensure basket and cubicle is getting enough
        if(BasketList.size() + thread.getPerson_Basket() >0 && CubicleList.size()>0 )
        {
            //gives arrival a new basket
            if(thread.getPerson_Basket()==0)
            {
                thread.newBasket= BasketManager.get(true).get(0);
                BasketList.remove(0);
            }
            thread.newCubicle= CubicleManager.get(true).get(0);
            CubicleList.remove(0);
        }
        else
        {
            Thread.currentThread().yield();
            return false;
        }

        return true;
    }
    public void waiting_dressing() {
        //randomly sleeping ( time for dressing )
        int sleepTime = (int) (Math.random()*10+1);
        try
        {
            if(Thread.currentThread().getPriority()==Thread.currentThread().MAX_PRIORITY)
            {
                System.out.println("a person : " + Thread.currentThread().getName()
                        + " has gotten a basket and a cubicle " + "and will occupy cubicle at "
                        + sleepTime + "seconds"+"(e)(f)");
            }
            else
                System.out.println("a person : " + Thread.currentThread().getName()
                        + " has gotten a basket and a cubicle " + "and will occupy cubicle at "
                        + sleepTime + "seconds"+"(a)(b)(c)");

            Thread.currentThread().sleep(1000*sleepTime);
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
class Person extends Thread {
    Pool newPool =null;
    Pool newBasket =null;
    Pool newCubicle =null;
    private int basket=0;

    Person(Pool pool)
    {
        this.newPool = pool;
    }
    public int getPerson_Basket() {return this.basket;}


    public void run() {
        //(a)(b)(c) returns cubicle
        while(!newPool.distribution(this));
        newCubicle.get();
        basket++; //arrival is given a new basket

        //(d)
        System.out.println("a person : " + Thread.currentThread().getName()
                + " is swiming" + "(d)" );

        //depature is given a high priority
        this.setPriority(MAX_PRIORITY);

        //(e)(f) returns basket and cubicle
        while(!newPool.distribution(this));
        newBasket.get();
        newCubicle.get();
        basket--;//depature returns a basket
        System.out.println("a person : " + Thread.currentThread().getName()
                + " has departed from pool" + "(complete)");
        this.stop();//stops thread
    }
}
//check if threads stopped ( every 5 seconds )
class terminal extends Thread{
    List<Thread> list=null;
    boolean judge=true;
    terminal(List<Thread> list){
        this.list=list;
    }
    public void run() {
        while(judge)
        {
            try {
                this.sleep(5000);
                for(Thread t:this.list)
                {
                    judge=t.isAlive();
                    if(judge==true)
                        break;
                }
                if(judge==false)
                {
                    System.out.println("has done");// if other threads have finished , println "has done"
                    this.stop();
                }
            }catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
public class SP {
    public static void main(String[] args) throws InterruptedException
    {
        Pool pool=new Pool();

        List<Thread> list =new ArrayList<>();
        pool.setManager();//manage threads' baskets and cubicles states
        for(int n=0;n<30;n++)
        {
            Thread thread = new Person(pool);
            list.add(thread);
        }
        for(Thread l :list)
        {
            l.start();
        }
        Thread viewer=new terminal(list);//additional thread for checking other threads

        viewer.start();
    }
}