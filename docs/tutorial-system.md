# In-Game Tutorial System (Aura)

A lightweight tutorial flow is now available through `UAuraTutorialSubsystem` and `UAuraTutorialDataAsset`.

## What was added
- `FTutorialStep` struct for step content (`StepId`, `Title`, `Description`, display/completion metadata).
- `UAuraTutorialDataAsset` for authoring tutorials in the editor.
- `UAuraTutorialSubsystem` (`GameInstanceSubsystem`) that can:
  - Start/stop tutorials
  - Move between steps
  - Broadcast events for UI widgets

## Recommended setup in Blueprints
1. Create a new data asset of type `AuraTutorialDataAsset`.
2. Fill in `TutorialId`, `DisplayName`, and `Steps`.
3. In your HUD/UI bootstrap Blueprint:
   - `Get Game Instance Subsystem` -> `AuraTutorialSubsystem`
   - Bind to:
     - `OnTutorialStarted`
     - `OnTutorialStepChanged`
     - `OnTutorialFinished`
4. On first player spawn or after movement unlock, call `StartTutorial` with your asset.
5. When player finishes an objective, call `CompleteCurrentStep`.

## Example flow
- Step 1: "Move" (WASD)
- Step 2: "Camera" (Mouse Look)
- Step 3: "Basic Attack" (LMB)
- Step 4: "Ability" (InputTag.1)

Each gameplay event should call `CompleteCurrentStep` only when the expected objective is met.

## Notes
- Subsystem is intentionally UI-agnostic; your widget decides how to render steps.
- If you need branching tutorials, use `GoToStep` from Blueprint logic based on conditions.
- For persistence (skip already-completed tutorial), store `TutorialId` completion in your save-game class.
