//package gh.funthomas424242.forge.addon;
//
//import org.jboss.forge.addon.ui.annotation.Command;
//import org.jboss.forge.addon.ui.input.UIPrompt;
//import org.jboss.forge.addon.ui.result.Result;
//import org.jboss.forge.addon.ui.result.Results;
//
//public class PromptCommand
//{
//
//   @Command(value = "prompt-boolean", help = "Prompts for information")
//   public Result promptBoolean(UIPrompt prompt)
//   {
//      boolean answer = prompt.promptBoolean("Do you love Forge 2?");
//      return Results.success("You answered: " + answer);
//   }
//
//   @Command(value = "prompt-boolean-false", help = "Prompts for information")
//   public Result promptBooleanFalse(UIPrompt prompt)
//   {
//      boolean answer = prompt.promptBoolean("Do you love Forge 2?", false);
//      return Results.success("You answered: " + answer);
//   }
//
//   @Command(value = "prompt-secret", help = "Prompts for information")
//   public Result promptSecret(UIPrompt prompt)
//   {
//      String answer = prompt.promptSecret("Type your password: ");
//      return Results.success("You answered: " + answer);
//   }
//
//   @Command(value = "prompt", help = "Prompts for information")
//   public Result prompt(UIPrompt prompt)
//   {
//      String answer = prompt.prompt("Type something: ");
//      return Results.success("You answered: " + answer);
//   }
//
//}