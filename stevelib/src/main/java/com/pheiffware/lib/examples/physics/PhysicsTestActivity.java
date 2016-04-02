package com.pheiffware.lib.examples.physics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.pheiffware.lib.R;
import com.pheiffware.lib.examples.physics.testScenarios.BouncingBall;
import com.pheiffware.lib.examples.physics.testScenarios.CompressedStackedObjects;
import com.pheiffware.lib.examples.physics.testScenarios.ConstrainedStackedObjects;
import com.pheiffware.lib.examples.physics.testScenarios.ConstrainedStackedObjectsDrop;
import com.pheiffware.lib.examples.physics.testScenarios.Elevator;
import com.pheiffware.lib.examples.physics.testScenarios.ElevatorWithLoad;
import com.pheiffware.lib.examples.physics.testScenarios.GeneralScenario1;
import com.pheiffware.lib.examples.physics.testScenarios.GeneralScenario2;
import com.pheiffware.lib.examples.physics.testScenarios.PolygonScenario;
import com.pheiffware.lib.examples.physics.testScenarios.PoolScenario;
import com.pheiffware.lib.examples.physics.testScenarios.SingleBallOnRamp;
import com.pheiffware.lib.examples.physics.testScenarios.SingleBallSitGround;
import com.pheiffware.lib.examples.physics.testScenarios.StackedObjects;
import com.pheiffware.lib.physics.entity.Entity;
import com.pheiffware.lib.simulation.SimulationRunner;

import java.util.List;

//TODO: Make fragment and manage display/size
public class PhysicsTestActivity extends AppCompatActivity
{
    private TestPhysicsView testView;
    private SimulationRunner<List<Entity>> simulationRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int numSteps = 3000;
        // @formatter:off
        TestPhysicsScenario[] physicsScenarios = new TestPhysicsScenario[]
                {new PolygonScenario(5.0f, numSteps), new StackedObjects(3.0f, numSteps, 40.5f, 500.5f, 20, 5, 800, 0.9f),
                        new CompressedStackedObjects(8.0f, numSteps, 40.5f, 500.5f, 20, 5, 800, 0.9f, 2500.0f, 300, 50),
                        new ConstrainedStackedObjectsDrop(5.0f, numSteps, 40.5f, 500.5f, 20, 8, 800, 0.9f),
                        new ConstrainedStackedObjects(3.0f, numSteps, 40.5f, 500.5f, 20, 7, 800, 0.9f),
                        new PoolScenario(1.5f, numSteps, 40, 500, 20, 5, 0.9f), new ElevatorWithLoad(1.5f, numSteps), new BouncingBall(1.0f, numSteps),
                        new Elevator(1.0f, numSteps), new SingleBallSitGround(), new GeneralScenario1(3.0f, numSteps), new GeneralScenario2(8.0f, numSteps),
                        new SingleBallOnRamp(3.0f, numSteps)};
        // @formatter:on

        simulationRunner = new TestPhysicsMultiSimulationRunner(1.6, true, physicsScenarios);

        testView = new TestPhysicsView(this, simulationRunner);
        setContentView(R.layout.activity_physics);
        LinearLayout rootView = (LinearLayout) findViewById(R.id.root);
        rootView.addView(testView, 0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        simulationRunner.start();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        simulationRunner.stop();
        //TODO: Create stopping/restarting mechanism
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
