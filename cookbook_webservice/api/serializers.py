from rest_framework import serializers
from rest_framework.fields import Field

from webportal.models import *


class TagSerializer(serializers.ModelSerializer):
    class Meta:
        model = Tag
        fields = ('id', 'name')

class UnitSerializer(serializers.ModelSerializer):
    class Meta:
        model = Unit
        fields = ('id', 'name', 'short')

class IngredientSerializer(serializers.ModelSerializer):
    unit = serializers.StringRelatedField(source='unit.short')
    class Meta:
        model = Ingredient
        fields = ('id', 'name', 'amount', 'unit')

class WorkStepSerializer(serializers.ModelSerializer):
    class Meta:
        model = WorkStep
        fields = ('id', 'description')

class RecipeSerializer(serializers.ModelSerializer):
    imageurl = serializers.StringRelatedField(source='image.url')
    workSteps = serializers.SerializerMethodField()
    class Meta:
        model = Recipe
        fields = ('id', 'name', 'description', 'ingredients', 'work_steps', 'workSteps', 'tags', 'imageurl')

    def get_workSteps(self, obj):
        if isinstance(obj, Recipe):
            steps = []
            for step in obj.get_work_steps():
                steps.append(step.id)
            return steps
        return None

class SessionSerializer(serializers.ModelSerializer):
    recipeName = serializers.StringRelatedField(source='recipe.name')
    currentStep = serializers.StringRelatedField(source='current_step')
    class Meta:
        model = CookingSession
        fields = ('id', 'recipe', 'recipeName', 'currentStep')
