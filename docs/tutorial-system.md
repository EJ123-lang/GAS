# In-Game Tutorial Quest System (Aura)

The tutorial system now supports **quest-style objectives** per step.

## Core pieces
- `FTutorialObjective`: one quest objective with `ObjectiveId`, `Description`, and `RequiredCount`.
- `FTutorialStep`: a tutorial step containing `Objectives` plus behavior flags.
- `UAuraTutorialDataAsset`: author complete tutorial flows in content.
- `UAuraTutorialSubsystem`: runtime controller for progress, completion, and UI events.

## Step behavior flags
Each `FTutorialStep` now supports:
- `bRequireExplicitCompletion`
  - `true`: objectives can complete, but step only advances when `CompleteCurrentStep()` is called.
  - `false`: step can auto-advance when objectives complete.
- `bAutoAdvanceWhenObjectivesComplete`
  - If enabled (and explicit completion is not required), subsystem automatically advances.
- `MinDisplayTime`
  - Prevents finishing too early; objective completion still waits until minimum time elapses.

## Runtime API (Blueprint)
From `AuraTutorialSubsystem`:
- `StartTutorial`
- `ReportObjectiveProgress(ObjectiveId, Delta)`
- `GetObjectiveProgress`
- `IsCurrentStepObjectivesComplete`
- `CompleteCurrentStep`
- `AdvanceToNextStep`
- `GoToStep`
- `StopTutorial`

## Events for UI / Quest Tracker
Bind your widget or HUD to:
- `OnTutorialStarted`
- `OnTutorialStepChanged`
- `OnTutorialObjectiveProgress`
- `OnTutorialFinished`

## Recommended Blueprint wiring
1. Create `AuraTutorialDataAsset` with steps/objectives.
2. On game start or first spawn:
   - `Get Game Instance Subsystem (AuraTutorialSubsystem)`
   - bind events
   - call `StartTutorial`
3. On gameplay events, report progress:
   - movement detected -> `ReportObjectiveProgress("Move", 1)`
   - attack landed -> `ReportObjectiveProgress("Attack", 1)`
   - ability cast -> `ReportObjectiveProgress("CastAbility", 1)`
4. If a step requires manual confirmation, call `CompleteCurrentStep` (e.g., after player clicks “Got it”).

## Example quest-like step
- Step: "Learn Basic Combat"
  - Objective `Move`: RequiredCount=1
  - Objective `Attack`: RequiredCount=3
  - Objective `CastAbility`: RequiredCount=1

This gives you a real tutorial-quest loop: objectives track incrementally and the step can auto-advance or wait for explicit confirmation.

For a complete beginner Blueprint + UI walkthrough, see `docs/tutorial-beginner-blueprint-ui.md`.
