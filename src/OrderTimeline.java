import java.util.*;
public final class OrderTimeline {
 public enum State { NEW, PLACED, RESERVED, SHIPPED, FAILED }
 public record Event(String id, String type) {}
 public static State reduce(List<Event> events) {
  State state=State.NEW; Set<String> seen=new HashSet<>();
  for(Event event:events){if(event.id()==null||event.id().isBlank())throw new IllegalArgumentException("event id required");if(!seen.add(event.id()))continue;
   state=switch(event.type()){
    case "placed" -> {if(state!=State.NEW)throw new IllegalArgumentException("invalid placement");yield State.PLACED;}
    case "reserved" -> {if(state!=State.PLACED)throw new IllegalArgumentException("reserve requires placed");yield State.RESERVED;}
    case "shipped" -> {if(state!=State.RESERVED)throw new IllegalArgumentException("ship requires reservation");yield State.SHIPPED;}
    case "failed" -> {if(state==State.SHIPPED||state==State.FAILED)throw new IllegalArgumentException("terminal state");yield State.FAILED;}
    default -> throw new IllegalArgumentException("unknown event");
   };
  }return state;
 }
 public static void main(String[] args){
  var events=List.of(new Event("1","placed"),new Event("1","placed"),new Event("2","reserved"),new Event("3","shipped"));
  if(reduce(events)!=State.SHIPPED)throw new AssertionError("timeline");
  try{reduce(List.of(new Event("1","shipped")));throw new AssertionError("invalid transition accepted");}catch(IllegalArgumentException expected){}
  System.out.println("OrderTimeline self-tests passed");
 }
}
