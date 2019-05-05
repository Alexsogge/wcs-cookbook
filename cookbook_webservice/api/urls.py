from os.path import basename

from django.conf.urls import url, include
from django.urls import path
from django.contrib.auth.models import User
from rest_framework import routers, serializers, viewsets
from api import views

router = routers.DefaultRouter()
router.register(r'tags', views.TagViewSet)
router.register(r'units', views.UnitViewSet)
router.register(r'ingredients', views.IngredientViewSet)
router.register(r'worksteps', views.WorkStepViewSet)
router.register(r'recipes', views.RecipeViewSet)


urlpatterns = [
    path('', include(router.urls)),
    path('recipe/<int:id>/', views.getRecipeSerialization),
    path('ingredient/<int:id>/', views.getIngredient),
    path('startsession/<int:recipe_id>', views.startNewSession),
    path('rest-auth/', include('rest_auth.urls')),
]