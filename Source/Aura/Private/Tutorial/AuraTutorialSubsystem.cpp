#include "Tutorial/AuraTutorialSubsystem.h"

#include "Tutorial/AuraTutorialDataAsset.h"

bool UAuraTutorialSubsystem::StartTutorial(UAuraTutorialDataAsset* InTutorial, const bool bRestartIfRunning)
{
	if (!InTutorial || InTutorial->Steps.Num() == 0)
	{
		return false;
	}

	if (ActiveTutorial && !bRestartIfRunning)
	{
		return false;
	}

	ActiveTutorial = InTutorial;
	CurrentStepIndex = 0;

	OnTutorialStarted.Broadcast(ActiveTutorial);
	BroadcastCurrentStep();
	return true;
}

bool UAuraTutorialSubsystem::CompleteCurrentStep()
{
	if (!ActiveTutorial || !ActiveTutorial->Steps.IsValidIndex(CurrentStepIndex))
	{
		return false;
	}

	return AdvanceToNextStep();
}

bool UAuraTutorialSubsystem::AdvanceToNextStep()
{
	if (!ActiveTutorial)
	{
		return false;
	}

	++CurrentStepIndex;
	if (!ActiveTutorial->Steps.IsValidIndex(CurrentStepIndex))
	{
		StopTutorial(true);
		return true;
	}

	BroadcastCurrentStep();
	return true;
}

bool UAuraTutorialSubsystem::GoToStep(const int32 StepIndex)
{
	if (!ActiveTutorial || !ActiveTutorial->Steps.IsValidIndex(StepIndex))
	{
		return false;
	}

	CurrentStepIndex = StepIndex;
	BroadcastCurrentStep();
	return true;
}

void UAuraTutorialSubsystem::StopTutorial(const bool bBroadcastFinished)
{
	ActiveTutorial = nullptr;
	CurrentStepIndex = INDEX_NONE;

	if (bBroadcastFinished)
	{
		OnTutorialFinished.Broadcast();
	}
}

bool UAuraTutorialSubsystem::GetCurrentStep(FTutorialStep& OutStep, int32& OutStepIndex) const
{
	if (!ActiveTutorial || !ActiveTutorial->Steps.IsValidIndex(CurrentStepIndex))
	{
		return false;
	}

	OutStep = ActiveTutorial->Steps[CurrentStepIndex];
	OutStepIndex = CurrentStepIndex;
	return true;
}

void UAuraTutorialSubsystem::BroadcastCurrentStep()
{
	if (!ActiveTutorial || !ActiveTutorial->Steps.IsValidIndex(CurrentStepIndex))
	{
		return;
	}

	OnTutorialStepChanged.Broadcast(ActiveTutorial->Steps[CurrentStepIndex], CurrentStepIndex);
}
