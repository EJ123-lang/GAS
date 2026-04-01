#include "Tutorial/AuraTutorialSubsystem.h"

#include "HAL/PlatformTime.h"
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
	StepStartTimeSeconds = FPlatformTime::Seconds();
	ResetObjectiveProgressForCurrentStep();

	OnTutorialStarted.Broadcast(ActiveTutorial);
	BroadcastCurrentStep();
	return true;
}

bool UAuraTutorialSubsystem::CompleteCurrentStep()
{
	if (!GetCurrentStepPtr())
	{
		return false;
	}

	if (!IsMinimumDisplayTimeMet())
	{
		return false;
	}

	if (!IsCurrentStepObjectivesComplete())
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

	StepStartTimeSeconds = FPlatformTime::Seconds();
	ResetObjectiveProgressForCurrentStep();
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
	StepStartTimeSeconds = FPlatformTime::Seconds();
	ResetObjectiveProgressForCurrentStep();
	BroadcastCurrentStep();
	return true;
}

void UAuraTutorialSubsystem::StopTutorial(const bool bBroadcastFinished)
{
	ActiveTutorial = nullptr;
	CurrentStepIndex = INDEX_NONE;
	CurrentObjectiveProgress.Reset();
	StepStartTimeSeconds = 0.0;

	if (bBroadcastFinished)
	{
		OnTutorialFinished.Broadcast();
	}
}

bool UAuraTutorialSubsystem::ReportObjectiveProgress(const FName ObjectiveId, const int32 Delta)
{
	const FTutorialStep* CurrentStep = GetCurrentStepPtr();
	if (!CurrentStep || ObjectiveId.IsNone() || Delta <= 0)
	{
		return false;
	}

	for (const FTutorialObjective& Objective : CurrentStep->Objectives)
	{
		if (Objective.ObjectiveId != ObjectiveId)
		{
			continue;
		}

		int32& CurrentValue = CurrentObjectiveProgress.FindOrAdd(ObjectiveId);
		CurrentValue = FMath::Min(CurrentValue + Delta, Objective.RequiredCount);
		OnTutorialObjectiveProgress.Broadcast(ObjectiveId, CurrentValue, Objective.RequiredCount);

		if (IsCurrentStepObjectivesComplete() && !CurrentStep->bRequireExplicitCompletion && CurrentStep->bAutoAdvanceWhenObjectivesComplete && IsMinimumDisplayTimeMet())
		{
			AdvanceToNextStep();
		}

		return true;
	}

	return false;
}

bool UAuraTutorialSubsystem::IsCurrentStepObjectivesComplete() const
{
	const FTutorialStep* CurrentStep = GetCurrentStepPtr();
	if (!CurrentStep)
	{
		return false;
	}

	for (const FTutorialObjective& Objective : CurrentStep->Objectives)
	{
		const int32* CurrentValue = CurrentObjectiveProgress.Find(Objective.ObjectiveId);
		if (!CurrentValue || *CurrentValue < Objective.RequiredCount)
		{
			return false;
		}
	}

	return true;
}

bool UAuraTutorialSubsystem::GetObjectiveProgress(const FName ObjectiveId, int32& OutCurrentCount, int32& OutRequiredCount) const
{
	const FTutorialStep* CurrentStep = GetCurrentStepPtr();
	if (!CurrentStep)
	{
		return false;
	}

	for (const FTutorialObjective& Objective : CurrentStep->Objectives)
	{
		if (Objective.ObjectiveId != ObjectiveId)
		{
			continue;
		}

		OutRequiredCount = Objective.RequiredCount;
		OutCurrentCount = CurrentObjectiveProgress.FindRef(ObjectiveId);
		return true;
	}

	return false;
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
	const FTutorialStep* CurrentStep = GetCurrentStepPtr();
	if (!CurrentStep)
	{
		return;
	}

	OnTutorialStepChanged.Broadcast(*CurrentStep, CurrentStepIndex);

	for (const FTutorialObjective& Objective : CurrentStep->Objectives)
	{
		OnTutorialObjectiveProgress.Broadcast(Objective.ObjectiveId, CurrentObjectiveProgress.FindRef(Objective.ObjectiveId), Objective.RequiredCount);
	}
}

void UAuraTutorialSubsystem::ResetObjectiveProgressForCurrentStep()
{
	CurrentObjectiveProgress.Reset();

	const FTutorialStep* CurrentStep = GetCurrentStepPtr();
	if (!CurrentStep)
	{
		return;
	}

	for (const FTutorialObjective& Objective : CurrentStep->Objectives)
	{
		CurrentObjectiveProgress.Add(Objective.ObjectiveId, 0);
	}
}

const FTutorialStep* UAuraTutorialSubsystem::GetCurrentStepPtr() const
{
	if (!ActiveTutorial || !ActiveTutorial->Steps.IsValidIndex(CurrentStepIndex))
	{
		return nullptr;
	}

	return &ActiveTutorial->Steps[CurrentStepIndex];
}

bool UAuraTutorialSubsystem::IsMinimumDisplayTimeMet() const
{
	const FTutorialStep* CurrentStep = GetCurrentStepPtr();
	if (!CurrentStep)
	{
		return false;
	}

	const double Elapsed = FPlatformTime::Seconds() - StepStartTimeSeconds;
	return Elapsed >= static_cast<double>(CurrentStep->MinDisplayTime);
}
