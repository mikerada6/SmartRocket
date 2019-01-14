import java.awt.*;
import java.util.ArrayList;

import static java.util.Arrays.sort;

public class Population {
    Rocket[] rockets;
    int popsize;
    ArrayList<Rocket> matingpool;

    public Population() {
        popsize = 5000;
        rockets = new Rocket[popsize];
        matingpool = new ArrayList<Rocket>();

        for (int i = 0; i < popsize; i++) {
            rockets[i] = new Rocket();
        }
    }

    public double evaluate()
    {
        double avgFit=0;
        double maxFit=0;
        sort(rockets);
        for(Rocket r: rockets){
            double fit = r.calcFitness();
            if(maxFit<fit)
            {
                maxFit=fit;
            }
            avgFit+=fit;
        }
        avgFit /= rockets.length;
        for(Rocket r: rockets)
        {
            r.setMatingEligibility(r.calcFitness()/maxFit);
        }

        matingpool = new ArrayList<Rocket>();

        for (Rocket r : rockets) {
            double n = r.getMatingEligibility()* 100;
            for (int j = 0; j < n; j++) {
                matingpool.add(r);
            }
        }

        return avgFit;
    }

    public void selection() {
        Rocket[] newRockets = new Rocket[popsize];
        for (int i = 0; i < rockets.length; i++) {
            DNA parentA = random(matingpool).getDna();
            DNA parentB = random(matingpool).getDna();
            DNA child = parentA.crossover(parentB);
            child.mutation();
            newRockets[i] = new Rocket(child);
        }

        this.rockets = newRockets;
    }

    private Rocket random(ArrayList<Rocket> list) {
        int r = (int) (Math.random() * (list.size()));
        return list.get(r);
    }

    public void checkBarriers(Barrier[] b)
    {
        for (Rocket r : rockets) {
            r.checkBarriers(b);
        }
    }

    public void update() {
        for (Rocket r : rockets) {
            r.update();
        }
        this.evaluate();
    }

    public Graphics draw(Graphics g) {
        for (Rocket r : rockets) {
            r.draw(g);
        }
        return g;
    }
}

