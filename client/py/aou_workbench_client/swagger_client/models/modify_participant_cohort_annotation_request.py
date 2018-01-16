# coding: utf-8

"""
    AllOfUs Workbench API

    The API for the AllOfUs workbench.

    OpenAPI spec version: 0.1.0
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from pprint import pformat
from six import iteritems
import re


class ModifyParticipantCohortAnnotationRequest(object):
    """
    NOTE: This class is auto generated by the swagger code generator program.
    Do not edit the class manually.
    """


    """
    Attributes:
      swagger_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    swagger_types = {
        'annotation_value_string': 'str',
        'annotation_value_integer': 'int',
        'annotation_value_timestamp': 'str'
    }

    attribute_map = {
        'annotation_value_string': 'annotationValueString',
        'annotation_value_integer': 'annotationValueInteger',
        'annotation_value_timestamp': 'annotationValueTimestamp'
    }

    def __init__(self, annotation_value_string=None, annotation_value_integer=None, annotation_value_timestamp=None):
        """
        ModifyParticipantCohortAnnotationRequest - a model defined in Swagger
        """

        self._annotation_value_string = None
        self._annotation_value_integer = None
        self._annotation_value_timestamp = None
        self.discriminator = None

        if annotation_value_string is not None:
          self.annotation_value_string = annotation_value_string
        if annotation_value_integer is not None:
          self.annotation_value_integer = annotation_value_integer
        if annotation_value_timestamp is not None:
          self.annotation_value_timestamp = annotation_value_timestamp

    @property
    def annotation_value_string(self):
        """
        Gets the annotation_value_string of this ModifyParticipantCohortAnnotationRequest.
        the value of this annotation if the type is string

        :return: The annotation_value_string of this ModifyParticipantCohortAnnotationRequest.
        :rtype: str
        """
        return self._annotation_value_string

    @annotation_value_string.setter
    def annotation_value_string(self, annotation_value_string):
        """
        Sets the annotation_value_string of this ModifyParticipantCohortAnnotationRequest.
        the value of this annotation if the type is string

        :param annotation_value_string: The annotation_value_string of this ModifyParticipantCohortAnnotationRequest.
        :type: str
        """

        self._annotation_value_string = annotation_value_string

    @property
    def annotation_value_integer(self):
        """
        Gets the annotation_value_integer of this ModifyParticipantCohortAnnotationRequest.
        the value of this annotation if the type in integer

        :return: The annotation_value_integer of this ModifyParticipantCohortAnnotationRequest.
        :rtype: int
        """
        return self._annotation_value_integer

    @annotation_value_integer.setter
    def annotation_value_integer(self, annotation_value_integer):
        """
        Sets the annotation_value_integer of this ModifyParticipantCohortAnnotationRequest.
        the value of this annotation if the type in integer

        :param annotation_value_integer: The annotation_value_integer of this ModifyParticipantCohortAnnotationRequest.
        :type: int
        """

        self._annotation_value_integer = annotation_value_integer

    @property
    def annotation_value_timestamp(self):
        """
        Gets the annotation_value_timestamp of this ModifyParticipantCohortAnnotationRequest.
        the value of this annotation if the type is date/timestamp.

        :return: The annotation_value_timestamp of this ModifyParticipantCohortAnnotationRequest.
        :rtype: str
        """
        return self._annotation_value_timestamp

    @annotation_value_timestamp.setter
    def annotation_value_timestamp(self, annotation_value_timestamp):
        """
        Sets the annotation_value_timestamp of this ModifyParticipantCohortAnnotationRequest.
        the value of this annotation if the type is date/timestamp.

        :param annotation_value_timestamp: The annotation_value_timestamp of this ModifyParticipantCohortAnnotationRequest.
        :type: str
        """

        self._annotation_value_timestamp = annotation_value_timestamp

    def to_dict(self):
        """
        Returns the model properties as a dict
        """
        result = {}

        for attr, _ in iteritems(self.swagger_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(map(
                    lambda item: (item[0], item[1].to_dict())
                    if hasattr(item[1], "to_dict") else item,
                    value.items()
                ))
            else:
                result[attr] = value

        return result

    def to_str(self):
        """
        Returns the string representation of the model
        """
        return pformat(self.to_dict())

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

    def __eq__(self, other):
        """
        Returns true if both objects are equal
        """
        if not isinstance(other, ModifyParticipantCohortAnnotationRequest):
            return False

        return self.__dict__ == other.__dict__

    def __ne__(self, other):
        """
        Returns true if both objects are not equal
        """
        return not self == other
