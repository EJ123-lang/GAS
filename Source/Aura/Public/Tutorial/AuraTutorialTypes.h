#pragma once

#include "CoreMinimal.h"
#include "AuraTutorialTypes.generated.h"

USTRUCT(BlueprintType)
struct FTutorialObjective
{
	GENERATED_BODY()

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	FName ObjectiveId = NAME_None;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	FText Description;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial", meta = (ClampMin = "1", UIMin = "1"))
	int32 RequiredCount = 1;
};

USTRUCT(BlueprintType)
struct FTutorialStep
{
	GENERATED_BODY()

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	FName StepId = NAME_None;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	FText Title;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial", meta = (MultiLine = true))
	FText Description;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	TArray<FTutorialObjective> Objectives;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	float MinDisplayTime = 0.f;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	bool bRequireExplicitCompletion = false;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	bool bAutoAdvanceWhenObjectivesComplete = true;
};
