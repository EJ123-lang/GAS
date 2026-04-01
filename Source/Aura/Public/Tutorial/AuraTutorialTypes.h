#pragma once

#include "CoreMinimal.h"
#include "AuraTutorialTypes.generated.h"

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
	float MinDisplayTime = 0.f;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	bool bRequireExplicitCompletion = true;
};
