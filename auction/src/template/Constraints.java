package template;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import logist.simulation.Vehicle;
import logist.task.Task;

public class Constraints {
  
  /**
   * 
   * @param a
   * @param nbrTasks the number of tasks there should be
   * @return
   */
  public static boolean checkAllConstraints(Assignment a, int nbrTasks){
    if(! checkActionVehicleConstraint(a)){
      return false;
    }
    
    else if(! checkAllTasksMustBeDoneConstraint(a, nbrTasks)){
      return false;
    }
    
    else if(! checkFirstActionTime1Constraint(a)){
      return false;
    }
    
    else if(! checkNextActionSameVehicleConstraint(a)){
      return false;
    }
    
    else if(! checkNoVehicleOverloadedConstraint(a)){
      return false;
    }
    
    else if(! checkPickupBeforeDeliveryConstraint(a)){
      return false;
    }
    
    else{
      return true;
    }
    
  }
  
  /**
   * check if every task assigned to a vehicle is also a task of this vehicle
   * 
   * @param assignment
   * @return
   */
  public static boolean checkActionVehicleConstraint(Assignment assignment) {
    for(Vehicle vehicle : assignment.vehicleRoutes.keySet()){
      for(Action action : assignment.vehicleRoutes.get(vehicle)){
        if(! assignment.vehicles.get(action.task).equals(vehicle)){
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * all tasks must be picked up and delivered: the set of values of the
   * variables in the union of the nextAction and the firstAction must be equal
   * to the set of the PickUp and Delivery for all elements of the set of tasks
   * T plus Nv times the value NULL.
   * 
   * In other words: The number of NULL in next action must be the same as nbr
   * of vehicules where firstAction(v) != NULL.
   * 
   * @param assignment
   * @return
   */
  public static boolean checkAllTasksMustBeDoneConstraint(Assignment assignment, int nbrTasks) {
    int nbrActions = 0;
    for(List<Action> l : assignment.vehicleRoutes.values()){
      nbrActions += l.size();
    }
    if(nbrActions % 2 != 0){
      return false;
    }
    
    return nbrActions/2 == nbrTasks;
  }
  
  /**
   * indexes are correct
   * @param assignment
   * @return
   */
  //TODO est ce que c'est utile???
  public static boolean checkFirstActionTime1Constraint(Assignment assignment) {
    for(List<Action> l : assignment.vehicleRoutes.values()){
      int index = 0;
      for(Action act : l){
        if(assignment.indexOf.get(act) != index++){
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * all actions in a route belong to the correct vehicle
   * @param assignment
   * @return
   */
  public static boolean checkNextActionSameVehicleConstraint(Assignment assignment) {
    for(Entry<Vehicle, List<Action>> e : assignment.vehicleRoutes.entrySet()){
      Vehicle v = e.getKey();
      for(Action act : e.getValue()){
        if(! assignment.vehicles.get(act.task).equals(v)){
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * load(a_i @ PickUp) > freeload(v_k) => vehicle(a_i) != v_k  
   * @param assignment
   * @return
   */
  public static boolean checkNoVehicleOverloadedConstraint(Assignment assignment) {
    for(Vehicle vehicle : assignment.vehicleRoutes.keySet()){
      if(! checkVehicleOverloadConstraint(assignment, vehicle)){
        return false;
      }
    }
    return true;
  }
  
  public static boolean checkVehicleOverloadConstraint(Assignment assignment, Vehicle vehicle){
    double freeLoad = vehicle.capacity();
    for(Action act : assignment.vehicleRoutes.get(vehicle)){
      if(act.isPickup()){
        freeLoad -= act.task.weight;
      }else{
        freeLoad += act.task.weight;
      }
      if(freeLoad < 0){
        return false;
      }
    }
    return true;
  }
  
  /**
   * times(Delivery(t_1)) > times(PickUp(t_1))
   * @param assignment
   * @return
   */
  public static boolean checkPickupBeforeDeliveryConstraint(Assignment assignment) {
    
    for(List<Action> l : assignment.vehicleRoutes.values()){
      HashSet<Task> set = new HashSet<Task>();
      for(Action act : l){
        if(act.isPickup()){
          if(! set.add(act.task)){
            return false;
          }
        }else{
          if(set.add(act.task)){
            return false;
          }
        }
      }
    }
    return true;
  }
  
}

