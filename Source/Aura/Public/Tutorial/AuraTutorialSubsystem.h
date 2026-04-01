#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Tutorial/AuraTutorialTypes.h"
#include "AuraTutorialSubsystem.generated.h"

class UAuraTutorialDataAsset;

DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FTutorialStartedSignature, UAuraTutorialDataAsset*, Tutorial);
DECLARE_DYNAMIC_MULTICAST_DELEGATE_TwoParams(FTutorialStepChangedSignature, const FTutorialStep&, Step, int32, StepIndex);
DECLARE_DYNAMIC_MULTICAST_DELEGATE(FTutorialFinishedSignature);
DECLARE_DYNAMIC_MULTICAST_DELEGATE_ThreeParams(FTutorialObjectiveProgressSignature, FName, ObjectiveId, int32, CurrentCount, int32, RequiredCount);

UCLASS()
class AURA_API UAuraTutorialSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()

public:
	UFUNCTION(BlueprintCallable, Category = "Tutorial")
	bool StartTutorial(UAuraTutorialDataAsset* InTutorial, bool bRestartIfRunning = false);

	UFUNCTION(BlueprintCallable, Category = "Tutorial")
	bool CompleteCurrentStep();

	UFUNCTION(BlueprintCallable, Category = "Tutorial")
	bool AdvanceToNextStep();

	UFUNCTION(BlueprintCallable, Category = "Tutorial")
	bool GoToStep(int32 StepIndex);

	UFUNCTION(BlueprintCallable, Category = "Tutorial")
	void StopTutorial(bool bBroadcastFinished);

	UFUNCTION(BlueprintCallable, Category = "Tutorial")
	bool ReportObjectiveProgress(FName ObjectiveId, int32 Delta = 1);

	UFUNCTION(BlueprintPure, Category = "Tutorial")
	bool IsCurrentStepObjectivesComplete() const;

	UFUNCTION(BlueprintPure, Category = "Tutorial")
	bool GetObjectiveProgress(FName ObjectiveId, int32& OutCurrentCount, int32& OutRequiredCount) const;

	UFUNCTION(BlueprintPure, Category = "Tutorial")
	bool IsTutorialRunning() const { return ActiveTutorial != nullptr; }

	UFUNCTION(BlueprintPure, Category = "Tutorial")
	UAuraTutorialDataAsset* GetActiveTutorial() const { return ActiveTutorial; }

	UFUNCTION(BlueprintPure, Category = "Tutorial")
	bool GetCurrentStep(FTutorialStep& OutStep, int32& OutStepIndex) const;

	UPROPERTY(BlueprintAssignable, Category = "Tutorial")
	FTutorialStartedSignature OnTutorialStarted;

	UPROPERTY(BlueprintAssignable, Category = "Tutorial")
	FTutorialStepChangedSignature OnTutorialStepChanged;

	UPROPERTY(BlueprintAssignable, Category = "Tutorial")
	FTutorialObjectiveProgressSignature OnTutorialObjectiveProgress;

	UPROPERTY(BlueprintAssignable, Category = "Tutorial")
	FTutorialFinishedSignature OnTutorialFinished;

private:
	void BroadcastCurrentStep();
	void ResetObjectiveProgressForCurrentStep();
	const FTutorialStep* GetCurrentStepPtr() const;
	bool IsMinimumDisplayTimeMet() const;

	UPROPERTY(Transient)
	TObjectPtr<UAuraTutorialDataAsset> ActiveTutorial = nullptr;

	UPROPERTY(Transient)
	int32 CurrentStepIndex = INDEX_NONE;

	UPROPERTY(Transient)
	TMap<FName, int32> CurrentObjectiveProgress;

	double StepStartTimeSeconds = 0.0;
};
