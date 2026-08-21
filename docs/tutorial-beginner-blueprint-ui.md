# Beginner Guide: Build an In-Game Tutorial Quest UI (Blueprint, Step by Step)

This guide shows exactly how to use the Aura tutorial quest system in Blueprints.

---

## 0) What you will build
By the end, you will have:
1. A `AuraTutorialDataAsset` with tutorial steps/objectives.
2. A Widget Blueprint that shows:
   - Step title/description
   - Objective progress (example: `Attack 1 / 3`)
3. A Blueprint setup that starts the tutorial at runtime.
4. Gameplay Blueprints that report objective progress (Move/Attack/Ability).

---

## 1) Create the tutorial data asset

1. In Content Browser, right-click → **Miscellaneous** → **Data Asset**.
2. Pick class: **AuraTutorialDataAsset**.
3. Name it: `DA_Tutorial_Beginner`.
4. Open the asset and fill:
   - `TutorialId`: `Tutorial.Beginner`
   - `DisplayName`: `Beginner Tutorial`
   - `Steps`: add 2–4 steps.

### Example step setup

### Step 1
- `StepId`: `Step.Move`
- `Title`: `Move Around`
- `Description`: `Use WASD to move your character.`
- `MinDisplayTime`: `1.0`
- `bRequireExplicitCompletion`: `false`
- `bAutoAdvanceWhenObjectivesComplete`: `true`
- `Objectives`:
  - Objective 1:
    - `ObjectiveId`: `Move`
    - `Description`: `Move once`
    - `RequiredCount`: `1`

### Step 2
- `StepId`: `Step.Attack`
- `Title`: `Basic Attack`
- `Description`: `Use Left Mouse Button to attack 3 times.`
- `Objectives`:
  - `ObjectiveId`: `Attack`
  - `RequiredCount`: `3`

### Step 3
- `StepId`: `Step.Ability`
- `Title`: `Cast Ability`
- `Description`: `Press 1 to cast your first ability.`
- `Objectives`:
  - `ObjectiveId`: `CastAbility`
  - `RequiredCount`: `1`

---

## 2) Create the tutorial UI widget

1. Right-click Content Browser → **User Interface** → **Widget Blueprint**.
2. Pick **UserWidget**, name it `WBP_TutorialQuest`.
3. Add widgets:
   - `TextBlock` named `StepTitleText`
   - `TextBlock` named `StepDescriptionText`
   - `VerticalBox` named `ObjectiveListBox`
   - Optional: `Button` named `ContinueButton` (for manual completion steps)

### Optional objective row widget (cleaner UI)
1. Create another widget: `WBP_TutorialObjectiveRow`.
2. Add one `TextBlock` named `ObjectiveProgressText`.
3. Add a function `SetObjectiveText(ObjectiveDescription, Current, Required)`.
4. Display text as: `"{ObjectiveDescription}: {Current}/{Required}"`.

---

## 3) Add tutorial UI logic (Event Graph)

In `WBP_TutorialQuest`:

### Variables
- `TutorialSubsystem` (type `AuraTutorialSubsystem` object reference)
- `CurrentStepIndex` (int)
- `CurrentStepData` (type `TutorialStep` / `FTutorialStep`)

### On Construct
1. Node: `Get Game Instance Subsystem` (Class = `AuraTutorialSubsystem`)
2. Save to `TutorialSubsystem` variable.
3. Bind events:
   - `Bind Event to OnTutorialStarted`
   - `Bind Event to OnTutorialStepChanged`
   - `Bind Event to OnTutorialObjectiveProgress`
   - `Bind Event to OnTutorialFinished`

### Event: OnTutorialStepChanged
Inputs: `Step`, `StepIndex`
1. Set `CurrentStepData` = `Step`
2. Set `CurrentStepIndex` = `StepIndex`
3. Set `StepTitleText` from `Step.Title`
4. Set `StepDescriptionText` from `Step.Description`
5. Clear `ObjectiveListBox`
6. For each objective in `Step.Objectives`:
   - Create row widget (`WBP_TutorialObjectiveRow`) OR create text dynamically
   - Query subsystem: `GetObjectiveProgress(ObjectiveId, Current, Required)`
   - Update row text
   - Add row to `ObjectiveListBox`

### Event: OnTutorialObjectiveProgress
Inputs: `ObjectiveId`, `CurrentCount`, `RequiredCount`
1. Find corresponding row in UI
2. Update text to `CurrentCount/RequiredCount`

### Event: OnTutorialFinished
1. Hide or remove tutorial widget from viewport.

### Continue Button (optional)
Use only when step has `bRequireExplicitCompletion = true`:
1. On Clicked → call `CompleteCurrentStep` on subsystem.

---

## 4) Start tutorial from HUD or Player Controller Blueprint

Best place: your HUD setup Blueprint or Player Controller BeginPlay.

1. `Create Widget` (`WBP_TutorialQuest`) and `Add to Viewport`.
2. `Get Game Instance Subsystem` (`AuraTutorialSubsystem`).
3. Call `StartTutorial`:
   - `InTutorial` = `DA_Tutorial_Beginner`
   - `bRestartIfRunning` = `false`

If `StartTutorial` returns false, print debug message (`Print String`).

---

## 5) Report objective progress from gameplay events

You must call `ReportObjectiveProgress` when the player does actions.

## Move objective example
In Player Character Blueprint:
- When movement input value is non-zero the first time:
  - `Get Game Instance Subsystem (AuraTutorialSubsystem)`
  - `ReportObjectiveProgress(ObjectiveId="Move", Delta=1)`
- Use a bool like `bTutorialMoveReported` to avoid duplicate spam.

## Attack objective example
Where you detect a successful basic attack (or montage notify):
- `ReportObjectiveProgress("Attack", 1)`

## Ability objective example
Where ability key successfully triggers cast:
- `ReportObjectiveProgress("CastAbility", 1)`

---

## 6) Manual step confirmation flow (optional)
If you want a “Click Continue” step:
1. Set step `bRequireExplicitCompletion = true`.
2. Keep objective(s) as usual.
3. Show `ContinueButton` only when objectives are done.
4. On click, call `CompleteCurrentStep()`.

---

## 7) Troubleshooting (quick)

## Tutorial not starting
- Ensure data asset has at least 1 step.
- Ensure each step has valid fields.
- Check BeginPlay actually calls `StartTutorial`.

## Objective never progresses
- Verify `ObjectiveId` string matches exactly (`Attack` vs `attack`).
- Confirm your gameplay event fires (add `Print String`).
- Confirm you are calling subsystem from the active game instance.

## UI does not update
- Ensure widget binds to delegates on Construct.
- Ensure widget is added to viewport.
- Ensure `OnTutorialObjectiveProgress` handler updates the correct row.

---

## 8) Beginner-friendly recommended first version
Keep it very simple first:
1. 3 steps only (`Move`, `Attack`, `CastAbility`).
2. Auto-advance on completion.
3. One TextBlock for title, one for description, one vertical list for objectives.
4. Add polish later (animations, sounds, highlight effects).

You can build this in under 1 hour and iterate from there.
