#pragma once

#include "CoreMinimal.h"
#include "Engine/DataAsset.h"
#include "Tutorial/AuraTutorialTypes.h"
#include "AuraTutorialDataAsset.generated.h"

UCLASS(BlueprintType)
class AURA_API UAuraTutorialDataAsset : public UPrimaryDataAsset
{
	GENERATED_BODY()

public:
	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	FName TutorialId = NAME_None;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	FText DisplayName;

	UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "Tutorial")
	TArray<FTutorialStep> Steps;
};
